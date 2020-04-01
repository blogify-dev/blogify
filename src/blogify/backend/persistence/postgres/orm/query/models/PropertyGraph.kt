package blogify.backend.persistence.postgres.orm.query.models

import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.PropMap

import kotlin.reflect.KClass

class PropertyPointer<TRoot : Resource, TContainer : Resource, out TValue : Any> (
    val parent: PropertyPointer<TRoot, *, TContainer>?,
    val handle: PropMap.PropertyHandle.Ok<TContainer>
)

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
 * @param pointers a set of [pointers][PropertyPointer] to construct the graph with. Container properties that are not used directly, (such as `Owner::cat`)
 *                 **cannot** be omitted, since their presence is still needed to link  to the root object.
 *
 * @author Benjozork
 */
class PropertyGraph<TRoot : Resource> (
    val klass: KClass<TRoot>,
    vararg val pointers: PropertyPointer<TRoot, *, *>
) {

    private val rootNode: RootNode
    val rootChildren: MutableSet<Node<*>> = mutableSetOf()

    init {
        rootNode = this.RootNode()
        pointers.forEach { rootNode.consume(it) }
    }


    abstract inner class INode (
        open val children: MutableSet<PropertyGraph<TRoot>.Node<*>>
    ) {
        abstract fun consume(pointer: PropertyPointer<*, *, *>): Boolean
    }

    inner class RootNode : INode(rootChildren) {
        override fun consume(pointer: PropertyPointer<*, *, *>): Boolean {
            return children.firstOrNull { it.consume(pointer) }?.let { true }
                ?: children.add(this@PropertyGraph.Node(this@RootNode, pointer))
        }
    }

    /**
     * A node of a [PropertyGraph]. Represents a single property of it's [parent] node.
     *
     * @param TNodeContainer the type of the container class of this node
     * @param parent         the parent node
     * @param handle         the [PropMap.PropertyHandle.Ok] of the property this node contains
     *
     * @property type the type of the property this node contains
     */
    inner class Node<TNodeContainer : Resource> (
        val parent: INode,
        val pointer: PropertyPointer<*, TNodeContainer, *>
    ) : INode(rootChildren) {

        val type: KClass<out Any> = pointer.handle.property.returnType.classifier as KClass<out Any>

        override val children: MutableSet<Node<*>> = mutableSetOf()

        /**
         * Try to propagate the [PropertyPointer] to the appropriate child [Node]. Throws an exception if no appropriate node
         * was found.
         */
        override fun consume(pointer: PropertyPointer<*, *, *>): Boolean {
            return if (pointer.handle.klass == this.type && pointer.parent == this.pointer) { // Is pointer pointing to a property of our class ?
                // Yes, add it to our children
                this.children.add(Node(this, pointer))
                true
            } else children.firstOrNull { it.consume(pointer) }?.let { true } ?: false
        }

    }

}
