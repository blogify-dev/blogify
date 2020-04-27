import reflect.models.Mapped
import java.util.*

open class NotResouce: Mapped() {
    open val uuid: UUID = UUID.randomUUID()
}