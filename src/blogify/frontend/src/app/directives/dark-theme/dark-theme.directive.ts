import { Directive, ElementRef} from '@angular/core';
import { DarkModeService} from "../../services/darkmode/dark-mode.service";

@Directive({
    selector: '[appDarkTheme]'
})
export class DarkThemeDirective {

    constructor(el: ElementRef, darkModeService: DarkModeService) {
        console.log(darkModeService.darkMode);
        darkModeService.darkMode.subscribe(it => {
            el.nativeElement.setAttribute("data-theme", it ? 'dark' : 'light');
        });
    }

}
