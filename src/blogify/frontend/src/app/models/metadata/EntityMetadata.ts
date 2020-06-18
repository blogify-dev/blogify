import { Kind } from '@blogify/models/metadata/Kind';

/**
 * Represents entity metadata
 */
export interface EntityMetadata {
    entity: {
        isSearchable: boolean
    }
    properties: { [k: string]: PropertyMetadata },
}

/**
 * Represents entity property metadata
 */
export interface PropertyMetadata {
    entity: {
        isVisible: boolean
        isUpdatable: boolean,
        kind: Kind
    },
    filtering: {
        isFilterable: boolean
    },
}
