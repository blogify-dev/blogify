package blogify.backend.util

import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi

import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import org.postgresql.util.PGobject


private val objectMapper = jacksonObjectMapper().apply {
    val resourceModule = SimpleModule()
    resourceModule.addSerializer(Resource.ResourceIdSerializer)

    registerModule(resourceModule)
}

inline fun <reified T : Any> Table.jsonb(name: String): Column<T> =
    registerColumn(name, Json(T::class.java))


class Json<out T : Any>(private val klass: Class<T>) : ColumnType() {
    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String
        stmt[index] = obj
    }

    override fun valueFromDB(value: Any): Any {
        value as PGobject
        return try {
            objectMapper.readValue(value.value, klass)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Can't parse JSON: $value")
        }
    }


    override fun notNullValueToDB(value: Any): Any = objectMapper.writeValueAsString(value)
    override fun nonNullValueToString(value: Any): String = "'${objectMapper.writeValueAsString(value)}'"
}
