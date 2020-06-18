export type KindType = 'String' | 'Number' | 'Boolean' | 'Entity'

/**
 * Represents an entity property kind
 */
export interface Kind {
    type: KindType,
    array: boolean,
}
