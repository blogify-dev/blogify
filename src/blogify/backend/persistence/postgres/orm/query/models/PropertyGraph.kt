package blogify.backend.persistence.postgres.orm.query.models

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap
import blogify.backend.resources.reflect.models.ext.unsafeOkHandle

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

import com.andreapivetta.kolor.red

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
class Pointer<TRoot : Resource, TContainer : Resource, out TValue : Any> (
    val parent: Pointer<TRoot, *, TContainer>?,
    val handle: PropMap.PropertyHandle.Ok<TContainer>
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

    override fun toString(): String = if (parent == null) "(${handle.klass.simpleName}) -> ${handle.name}" else (parent as Pointer<*, *, *>).toString() + " -> ${handle.name}"

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

    abstract inner class INode (
        open val children: MutableSet<PropertyGraph<TRoot>.Node<*>>
    ) {
        abstract fun consume(pointer: Pointer<TRoot, *, *>): Boolean
    }

    inner class RootNode : INode(rootChildren) {
        override fun consume(pointer: Pointer<TRoot, *, *>): Boolean {
            println("--- Consuming $pointer, visiting <root>")
            println("distances -> " +children.map { it.pointer.handle.name to pointer.distanceToAncestor(it.pointer) })
            return children.firstOrNull { it.consume(pointer) }?.let { true }
                ?: children.add(this@PropertyGraph.Node(this@RootNode, pointer)).also { println("adpoting $pointer") }
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

        val type: KClass<out Any> = pointer.handle.property.returnType.classifier as KClass<out Any>

        override val children: MutableSet<Node<*>> = mutableSetOf()

        /**
         * Try to propagate the [Pointer] to the appropriate child [Node]. Throws an exception if no appropriate node
         * was found.
         */
        override fun consume(pointer: Pointer<TRoot, *, *>): Boolean {
            println("--- Consuming $pointer, visiting <${this.pointer}>")
            println("distances -> " + children.map { it.pointer.handle.name to pointer.distanceToAncestor(it.pointer) })
            return if (pointer.handle.klass == this.type && pointer.parent == this.pointer) { // Is pointer pointing to a property of our class ?
                // Yes, add it to our children
                this.children.add(Node(this, pointer)).also { println("adpoting $pointer") }
                true
            } else children.firstOrNull { it.consume(pointer) }?.let { true } ?: false
        }

    }

}
