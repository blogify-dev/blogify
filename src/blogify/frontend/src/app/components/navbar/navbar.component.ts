import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth/auth.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

    darkMode: boolean;

    constructor(public authService: AuthService, private router: Router) {}

    ngOnInit() {
        console.log(this.authService.userToken);

        if (window.matchMedia("prefers-color-scheme: dark")) {
            this.darkMode = true;
            document.querySelector('#toggle-dark-mode').setAttribute("checked", "");
        }
    }

    async navigateToProfile() {
        const url = `/profile/${await this.authService.getUserUUIDFromToken(this.authService.userToken)}`;
        await this.router.navigateByUrl(url)
    }

    toggleDarkMode() {
        this.darkMode = !this.darkMode;
        document.querySelector("html").setAttribute("data-theme", this.darkMode ? "dark" : "light");
        console.log("changed to: " + this.darkMode);
    }

}
