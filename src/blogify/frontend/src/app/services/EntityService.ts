import { EntityMetadata } from '@blogify/models/metadata/EntityMetadata';

export abstract class EntityService {
    
    abstract getMetadata(): Promise<EntityMetadata>
    
}
