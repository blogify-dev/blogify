package blogify.backend.resources.models

import java.util.UUID

/**
 * Applies to objects that can be identified by a [UUID],
 *
 * @author Benjozork
 */
interface Identified {

    val uuid: UUID

}
