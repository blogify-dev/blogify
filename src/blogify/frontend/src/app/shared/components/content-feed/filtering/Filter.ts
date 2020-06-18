import { PropertyMetadata } from '@blogify/models/metadata/EntityMetadata';

export interface Filter {
    property: PropertyMetadata & { name: string }
}
