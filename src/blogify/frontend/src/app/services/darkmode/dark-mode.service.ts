import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class DarkModeService {
    private useDarkMode = new BehaviorSubject(true);

    darkMode = this.useDarkMode.asObservable();

    constructor() {
    }

    getDarkModeValue(): boolean {
        return this.useDarkMode.getValue()
    }

    setDarkMode(value: boolean) {
        this.useDarkMode.next(value)
    }
}
