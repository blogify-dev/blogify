import { Directive, ViewContainerRef } from '@angular/core';

/**
 * Indicates where [entity rendering components]{@link EntityRenderComponent} will be placed inside {@link ContentFeedComponent}
 */
@Directive({
    selector: '[bContentHost]'
})
export class ContentHostDirective {

    constructor(public viewContainerRef: ViewContainerRef) {}

}
