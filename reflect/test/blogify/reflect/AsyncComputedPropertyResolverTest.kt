package blogify.reflect

import blogify.reflect.computed.extensions.computed
import blogify.reflect.computed.models.ComputedPropContainer
import blogify.reflect.computed.resolveComputedProps
import blogify.reflect.computed.resolveComputedPropsAsync
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import kotlinx.coroutines.runBlocking

class AsyncComputedPropertyResolverTest {

    @Test fun should_properly_resolve_property_containers_asynchronously() {
        val testObject = object : Mapped() {
            val name = computed { "James" }
            val age = computed { 17 }
            val address = computed { "12394 W. Green Drive" }
        }

        val resolutions = runBlocking {
             resolveComputedPropsAsync(testObject)
        }

        val values = resolutions.map {
            it.key.name to (it.value.resolution as ComputedPropContainer.Resolution.Value<*>).value
        }.toMap()

        assertEquals("James", values["name"])
        assertEquals(17, values["age"])
        assertEquals("12394 W. Green Drive", values["address"])
    }

    @Test fun asynchronous_computed_property_resolution_should_be_faster() {
        val testObject = object : Mapped() {
            val makeProp = { computed { Thread.sleep((5L..15L).random()) } }

            val slow0 = makeProp()
            val slow2 = makeProp()
            val slow3 = makeProp()
            val slow4 = makeProp()
            val slow5 = makeProp()
            val slow6 = makeProp()
            val slow7 = makeProp()
            val slow8 = makeProp()
            val slow9 = makeProp()
            val slowX = makeProp()
        }

        val testObject2 = object : Mapped() {
            val makeProp = { computed { Thread.sleep((13L..15L).random()) } }

            val slow0 = makeProp()
            val slow2 = makeProp()
            val slow3 = makeProp()
            val slow4 = makeProp()
            val slow5 = makeProp()
            val slow6 = makeProp()
            val slow7 = makeProp()
            val slow8 = makeProp()
            val slow9 = makeProp()
            val slowX = makeProp()
        }

        testObject.propMap // Warm-up and cache propMap
        testObject2.propMap

        val blockingStart = System.currentTimeMillis()
        runBlocking {
            resolveComputedProps(testObject)
        }
        val blockingTime = System.currentTimeMillis() - blockingStart

        val asyncStart = System.currentTimeMillis()
        runBlocking {
            resolveComputedPropsAsync(testObject2)
        }
        val asyncTime = System.currentTimeMillis() - asyncStart

        println("Blocking Time : $blockingTime")
        println("Async Time : $asyncTime")

        assertTrue(blockingTime > asyncTime)
    }

}
