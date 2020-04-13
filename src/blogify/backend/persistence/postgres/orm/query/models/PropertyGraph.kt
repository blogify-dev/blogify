package blogify.backend.persistence.postgres.orm.query.models

import blogify.backend.persistence.postgres.orm.extensions.klass
import blogify.backend.persistence.postgres.orm.extensions.mappedTable
import blogify.backend.persistence.postgres.orm.extensions.mapping
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.unsafeOkHandle

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf

import com.andreapivetta.kolor.red
import org.jetbrains.exposed.sql.*

/**
 * Points to a specific [property handle][PropMap.PropertyHandle.Ok] in a class. Can specify a pointer to containing property via [parent].
 *
 * @param TRoot      the root type of the pointer. This represents the type that is at the top of the hierarchy.
 * @param TContainer the type of the class that contains the property this is referring to
 * @param TValue     the value type of the property this is referring to
 *
 * @author Benjozork
 *
 * @property parent if needed, the pointer to a property with type [TContainer], but in the same hierarchy (must have same [TRoot])
 * @property handle the [handle][PropMap.PropertyHandle.Ok] this is referring to
 */
open class Pointer<TRoot : Resource, TContainer : Resource, TValue : Any> (
    open val parent: Pointer<TRoot, *, TContainer>?,
    open val handle: PropMap.PropertyHandle.Ok<TContainer>
) {

    @Suppress("UNCHECKED_CAST")
    constructor(parent: Pointer<TRoot, *, TContainer>?, property: KProperty1<TContainer, TValue>) : this (
        parent, property.unsafeOkHandle() as? PropMap.PropertyHandle.Ok<TContainer>
            ?: error("fatal: can't find ok handle for property '${property.name}'".red())
    )

    private lateinit var cachedAncestors: List<Pointer<TRoot, *, *>>
    fun distanceToAncestor(pointer: Pointer<TRoot, *, *>): Int {
        if (!::cachedAncestors.isInitialized) cachedAncestors = {
            val acc = mutableListOf<Pointer<TRoot, *, *>>()
            var nextParent: Pointer<TRoot, *, *>? = this.parent
            while (nextParent != null) {
                acc.add(nextParent)
                nextParent = nextParent.parent
            }
            acc
        }()

        return cachedAncestors.indexOf(pointer).takeIf { it != -1 }?.plus(1) ?: -1
    }

    override fun toString(): String =
        if (parent == null) "(${handle.klass.simpleName}) -> ${handle.name}" else (parent as Pointer<*, *, *>).toString() + " -> ${handle.name}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Pointer<*, *, *>) return false

        if (parent != other.parent) return false
        if (handle != other.handle) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parent?.hashCode() ?: 0
        result = 31 * result + handle.hashCode()
        return result
    }

}

class CollectionPointer<TRoot : Resource, TContainer : Resource, TCollectionElement : Resource> (
    override val parent: Pointer<TRoot, *, TContainer>?,
    override val handle: PropMap.PropertyHandle.Ok<TContainer>
) : Pointer<TRoot, TContainer, TCollectionElement>(parent, handle) {

    @Suppress("UNCHECKED_CAST")
    constructor(parent: Pointer<TRoot, *, TContainer>?, property: KProperty1<TContainer, Collection<TCollectionElement>>) : this (
        parent, property.unsafeOkHandle() as? PropMap.PropertyHandle.Ok<TContainer>
            ?: error("fatal: can't find ok handle for property '${property.name}'".red())
    )

}

/**
 * Graph that represents a data hierarchy to select for a query.
 *
 * **Every** node of the graph represents a single property of its parent node's class, and possibly contains
 * nodes representing some of its class' own properties (see [PropertyGraph.Node.children]).
 *
 * Example :
 *
 * ```
 *         | -> owner | -> name
 * House - |          | -> cat - | -> name
 *         | -> ...              | -> breed
 * ```
 *
 * @param TRoot    the type of the root class
 * @param klass    the class of the root of the graph, in the example above, `House`.
 *
 * @param pointers a set of [pointers][Pointer] to construct the graph with. Container properties that are not used directly, (such as `Owner::cat`)
 *                 cannot be omitted for resolve performance reasons.
 *
 * @author Benjozork
 *
 * @property rootChildren properties of [TRoot] that are located directly at the root
 */
class PropertyGraph<TRoot : Resource> (
    val klass: KClass<TRoot>,
    private vararg val pointers: Pointer<TRoot, *, *>
) {

    val rootNode: RootNode
    val rootChildren: MutableSet<Node<*>> = mutableSetOf()

    init {
        rootNode = this.RootNode()
        pointers.forEach { rootNode.consume(it) }
    }

    /**
     * Converts this property graph to a [Join], Visits each child node and joins the necessary tables, aliasing all them to prevent collisions
     * in recursive relationships.
     */
    fun toJoin(): Join {
        var join = Join(this.klass.mappedTable)

        this.rootChildren.forEach { join = it.joinWith(join) }

        return join
    }

    /**
     * Converts this property graph to a slice that only contains the columns that contains values requested via input pointers to primitive properties.
     */
    fun toSlice(): FieldSet {
        val join = toJoin()

        val inputValueMappingColumns = pointers.mapNotNull { (it.handle.mapping as? PropertyMapping.ValueMapping)?.column }
        val fieldsToKeep = join.fields.filter {
            if (it is Column<*>) {
                if (it.table is Alias<*>)
                    (it.table as Alias<*>).delegate.columns.first { c -> // We cannot use `Alias::operator get`, for some reason it doesn't work
                        c.name == it.name &&
                                c.columnType == it.columnType
                    } in inputValueMappingColumns
                else it in inputValueMappingColumns
            } else false
        }

        return join.slice(fieldsToKeep)
    }

    abstract inner class INode (
        open val children: MutableSet<PropertyGraph<TRoot>.Node<*>>
    ) {
        abstract fun consume(pointer: Pointer<TRoot, *, *>)
    }

    inner class RootNode : INode(rootChildren) {
        override fun consume(pointer: Pointer<TRoot, *, *>) {
            children.firstOrNull { pointer.distanceToAncestor(it.pointer) != -1 }
                ?.consume(pointer)
                ?: if (pointer.handle.klass == this@PropertyGraph.klass)
                    children.add(this@PropertyGraph.Node(this@RootNode, pointer))
                else error("fatal: pointer was not consumed".red())
        }
    }

    /**
     * A node of a [PropertyGraph]. Represents a single property of it's [parent] node.
     *
     * @param TNodeContainer the type of the container class of this node
     * @param parent         the parent node
     * @param pointer        the [Pointer] of the property this node contains
     *
     * @property type the type of the property this node contains
     */
    inner class Node<TNodeContainer : Resource> (
        val parent: INode,
        val pointer: Pointer<TRoot, TNodeContainer, *>
    ) : INode(rootChildren) {

        @Suppress("UNCHECKED_CAST")
        val type: KClass<out Any> = pointer.handle.property.returnType.let {
            if (pointer is CollectionPointer<*, *, *>)
                it.arguments.first().type?.klass() ?: error("")
            else it.klass()
        }

        override val children: MutableSet<Node<*>> = mutableSetOf()

        /**
         * Try to propagate the [Pointer] to the appropriate child [Node]. Throws an exception if no appropriate node
         * was found.
         */
        override fun consume(pointer: Pointer<TRoot, *, *>) {
            val pointerType = pointer.let {
                if (this is CollectionPointer<*, *, *>) {
                    it.handle.property.returnType.arguments.first().type?.klass<Any>()
                } else it.handle.klass
            }
            if (pointerType == this.type && pointer.parent == this.pointer) { // Is pointer pointing to a property of our class ?
                // Yes, add it to our children
                this.children.add(Node(this, pointer))
            } else children.firstOrNull { pointer.distanceToAncestor(it.pointer) != -1 }
                ?.consume(pointer)
                ?: error("fatal: pointer was not consumed".red())
        }

        /**
         * Joins this [Node] with the provided [join], accounting for its children as well
         */
        @Suppress("UNCHECKED_CAST")
        fun joinWith(join: Join): Join {
            return if (pointer is CollectionPointer) {
                val mapping = (this.pointer.handle.mapping as PropertyMapping.AssociativeMapping<*>)

                mapping.joinWith(join)
            } else {
                val resType = (this.type as KClass<Resource>)

                val joinedTable = resType.mappedTable.alias("joined_ptr_${pointer.hashCode().toString(16).replace('-', 'z')}")
                val joiningTable = if (pointer.parent != null)
                    pointer.handle.klass.mappedTable.alias("joined_ptr_${pointer.parent.hashCode().toString(16).replace('-', 'z')}")
                else pointer.handle.klass.mappedTable

                val otherColumn = (joinedTable.delegate as OrmTable<*>).identifyingColumn
                val onColumn = pointer.handle.klass.mappedTable.columns.first { it.referee == otherColumn }

                return if (this.children.all { !it.type.isSubclassOf(Resource::class) }) {
                    // Short-circuit and only join own class when all children are primitives
                    join.leftJoin(joinedTable, { if (joiningTable is Alias<Table>) joiningTable[onColumn] else onColumn }, { joinedTable[otherColumn] })
                } else {
                    // When some children are pointing to Resources, join them too
                    val newJoin = join.leftJoin(joinedTable, { onColumn }, { joinedTable[otherColumn] })

                    this.children.filter { child -> child.type.isSubclassOf(Resource::class) }
                        .fold(newJoin) { j, n -> n.joinWith(j) }
                }
            }

        }

    }

}
