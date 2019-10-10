import { Directive, ElementRef, Renderer2 } from '@angular/core';

@Directive({
  selector: '[appCompact]'
})
export class CompactDirective {

  constructor(private renderer: Renderer2, private el: ElementRef) {
      renderer.addClass(el.nativeElement, "no-padding")
  }

}
