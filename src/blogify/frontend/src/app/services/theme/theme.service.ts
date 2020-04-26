import { Injectable } from '@angular/core';
import { BehaviorSubject } from "rxjs";

const PREFERRED_THEME_KEY = 'preferredTheme'

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    private useDarkMode = new BehaviorSubject(true);


    constructor() {
        const theme = localStorage.getItem(PREFERRED_THEME_KEY)
        if (!theme) {
            // This is bad but we only use it on web so should be fine
            if (window.matchMedia('prefers-color-scheme: dark')) {
                this.setDarkMode(true)
            } else {
                this.setDarkMode(false)
            }
        } else {
            this.setDarkMode(theme == 'dark')
        }
    }

    private setDarkMode(value: boolean) {
        this.useDarkMode.next(value)
        localStorage.setItem(PREFERRED_THEME_KEY, value ? 'dark' : 'light')
    }

    toggleTheme() {
        this.setDarkMode(!this.useDarkMode.value)
    }

    get darkMode() {
        return this.useDarkMode.asObservable();
    }
}
