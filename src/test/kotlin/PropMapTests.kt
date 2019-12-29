import blogify.backend.annotations.Invisible
import blogify.backend.resources.User
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.valid
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

data class BruhICantName(val visible: String, @Invisible val invisible: String): Resource()

class PropMapTests {
    @Test
    fun `Only valid props should be picked`() {
        val propMap = BruhICantName::class.cachedPropMap().valid().values.toSet()
            .map { it.name }.toSet()
        val withInvisible =
            BruhICantName::class.declaredMemberProperties.filter { it.findAnnotation<Invisible>() == null }
                .map { it.name }
                .toSet()

        assertEquals(withInvisible, propMap, "The two should be the same")

    }
}