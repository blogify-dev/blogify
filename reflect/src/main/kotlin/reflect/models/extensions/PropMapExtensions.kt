package reflect.models.extensions

import filterThenMapValues
import reflect.models.PropMap

/**
 * Only returns [PropMap.PropertyHandle]s that respect interface [PropMap.PropertyHandle.Valid]
 *
 * @author Benjozork
 */
fun PropMap.valid() = this.map
    .filterThenMapValues (
        { it is PropMap.PropertyHandle.Valid },
        { it.value as PropMap.PropertyHandle.Valid }
    )

/**
 * Only returns [PropMap.PropertyHandle]s that are of type [PropMap.PropertyHandle.Ok]
 *
 * @author Benjozork
 */
fun PropMap.ok() = this.map
    .filterThenMapValues (
        { it is PropMap.PropertyHandle.Ok },
        { it.value as PropMap.PropertyHandle.Ok }
    )

/**
 * Only returns [PropMap.PropertyHandle]s that are of type [PropMap.PropertyHandle.AccessDenied]
 *
 * @author Benjozork
 */
fun PropMap.accessDenied() = this.map.filterThenMapValues ({ it is PropMap.PropertyHandle.AccessDenied }, { it.value as PropMap.PropertyHandle.AccessDenied })
