/**
 * Represents entity metadata
 */
export interface EntityMetadata {
    entity: {
        isSearchable: boolean
    }
    properties: { [k: string]: PropertyMetadata }
}

/**
 * Represents entity property metadata
 */
export interface PropertyMetadata {
    isVisible: boolean
    isUpdatable: boolean
}
