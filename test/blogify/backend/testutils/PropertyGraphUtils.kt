package blogify.backend.testutils

import blogify.backend.persistence.postgres.orm.query.models.CollectionPointer
import blogify.backend.persistence.postgres.orm.query.models.Pointer
import blogify.backend.persistence.postgres.orm.query.models.PropertyGraph

import com.andreapivetta.kolor.*

object PropertyGraphUtils {

    fun dumpPropertyGraph(graph: PropertyGraph<*>): String {
        val stream = StringBuilder()

        stream.append("[PropertyGraph ${graph.klass.simpleName!!.lightMagenta()}]")
        stream.append("\n|  Children :\n${graph.rootChildren.joinToString(separator = "\n") { dumpGraphNode(it).prependIndent("|\t\t") }}")

        return stream.toString()
    }

    fun dumpGraphNode(node: PropertyGraph<*>.Node<*>): String {
        val stream = StringBuilder()

        stream.append("[Node ${node.pointer.handle.name.lightMagenta()}]\n")
        stream.append("|  Pointer :  ${dumpPointer(node.pointer)}\n")
        stream.append("|  Property : ${node.pointer.handle.klass.simpleName!!.lightMagenta()}::${node.pointer.handle.name.lightGreen()}\n")
        stream.append("|  Children :" + if (node.children.isNotEmpty()) {
            "\n" + node.children.joinToString(separator = "\n") { dumpGraphNode(it).prependIndent("|\t\t")}
        } else " <empty>".yellow())

        return stream.toString()
    }

    fun dumpPointer(pointer: Pointer<*, *, *>): String {
        return if (pointer is CollectionPointer) {
            pointer.parent?.toString()?.let { "$it |> ${pointer.handle.name}".lightCyan() }
                ?: "(${pointer.handle.klass.simpleName}) |> ${pointer.handle.name}".lightCyan()
        } else "(${pointer.handle.klass.simpleName}) -> ${pointer.handle.name}".blue()
    }

}
