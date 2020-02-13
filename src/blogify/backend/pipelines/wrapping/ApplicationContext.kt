package blogify.backend.pipelines.wrapping

import blogify.backend.persistence.models.DataStore

/**
 * Context object that describes the application context, including i.e. data stores and configuration.
 *
 * @author Benjozork
 */
class ApplicationContext (
    val dataStore: DataStore
)
