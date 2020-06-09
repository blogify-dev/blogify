package blogify.backend.resources.reflect

import blogify.reflect.annotations.Hidden

import blogify.common.util.Wrap
import blogify.common.util.getOr
import blogify.common.util.never
import blogify.common.util.assertGet
import blogify.reflect.entity.construct
import blogify.backend.events.models.EventTarget
import blogify.backend.entity.Resource
import blogify.backend.util.*

import com.fasterxml.jackson.databind.ObjectMapper

import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InstantiatorTests {

    data class TestClass (
        val name: String,
        val age: Int = 25,
        @Hidden val password: String
    ) : Resource() {
        @Hidden
        override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    private val testObject =
        TestClass("abc", 17, "whatever")
    private val testData = mapOf (
        "name" to "abc",
        "age" to 17,
        "password" to "whatever"
    )

    @Test fun `instantiate should create object correctly`() {
        runBlocking {
            val propHandleDto = testData.mappedByHandles(TestClass::class, unsafe = true).getOr { never }
            val newInstance = TestClass::class.construct(propHandleDto, ObjectMapper())

            assertEquals(testObject, newInstance.get())
        }
    }

    private val testObjectDefaultAge =
        TestClass(
            name = "abc",
            password = "whatever"
        )
    private val testDataNoAge = mapOf (
        "name" to "abc",
        "password" to "whatever"
    )

    @Test fun `instantiate should create object correctly while omitting values for default properties`() {
        runBlocking {
            val propHandleDto = testDataNoAge.mappedByHandles(TestClass::class, unsafe = true).getOr { never }
            val newInstance = TestClass::class.construct(propHandleDto, ObjectMapper())

            assertEquals(testObjectDefaultAge, newInstance.get())
        }
    }

    data class OtherTestClass (
        val data: Int,
        val other: TestClass
    ) : Resource() {
        @Hidden
        override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    private val testObjectForOther =
        TestClass("me", 18, "hunter2")
    private val otherTestObject =
        OtherTestClass(1337, testObjectForOther)
    private val otherTestData = mapOf (
        "data" to 1337,
        "other" to testObjectForOther.uuid
    )

    @Test fun `instantiate should create object correctly with external fetching`() {
        runBlocking {
            val propHandleDto = otherTestData.mappedByHandles(OtherTestClass::class, unsafe = true).assertGet()
            val newInstance = OtherTestClass::class.construct(propHandleDto, ObjectMapper(), { _, _ -> Wrap { testObjectForOther } })

            assertEquals(otherTestObject, newInstance.get())
        }
    }

}
