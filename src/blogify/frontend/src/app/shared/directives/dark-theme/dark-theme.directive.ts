import { Directive, ElementRef } from '@angular/core';
import { ThemeService } from '@blogify/core/services/theme/theme.service';

@Directive({
    selector: '[appDarkTheme]'
})
export class DarkThemeDirective {

    constructor(el: ElementRef, themeService: ThemeService) {
        themeService.darkMode.subscribe(it => {
            el.nativeElement.setAttribute('data-theme', it ? 'dark' : 'light');
        });
    }

}
