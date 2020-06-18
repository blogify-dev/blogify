import { Entity } from '@blogify/models/entities/Entity';
import { Input } from '@angular/core';

export abstract class EntityRenderComponent<T extends Entity> {
    
    @Input() entity: T;

}
