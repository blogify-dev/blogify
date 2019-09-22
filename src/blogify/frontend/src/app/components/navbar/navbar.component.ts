import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AuthService} from "../../services/auth/auth.service";
import {Router} from "@angular/router";
import {DarkModeService} from "../../services/darkmode/dark-mode.service";


@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, AfterViewInit {

    @ViewChild("darkModeToggle", {static: false, read: ElementRef}) darkModeToggle: ElementRef;

    constructor(
        public authService: AuthService,
        private router: Router,
        private darkModeService: DarkModeService,
    ) {
    }

    ngOnInit() {
        console.log(this.authService.userToken);
    }

    ngAfterViewInit() {
        if (window.matchMedia("prefers-color-scheme: dark")) {
            this.darkModeService.setDarkMode(true);
            this.darkModeToggle.nativeElement.setAttribute("checked", "");
        }
    }

    async navigateToProfile() {
        const url = `/profile/${this.authService.userUUID}`;
        await this.router.navigateByUrl(url)
    }

    toggleDarkMode() {
        const darkMode = !this.darkModeService.getDarkModeValue();
        this.darkModeService.setDarkMode(darkMode)

    }

}
