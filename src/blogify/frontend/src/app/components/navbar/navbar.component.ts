import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';
import { Router } from '@angular/router';
import { DarkModeService } from '../../services/darkmode/dark-mode.service';


@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, AfterViewInit {

    @ViewChild('darkModeToggle', {static: false, read: ElementRef}) darkModeToggle: ElementRef;

    username: string;
    constructor(
        public authService: AuthService,
        private router: Router,
        private darkModeService: DarkModeService,
    ) {
    }

    ngOnInit() {
        if (this.authService.isLoggedIn()) {
            this.authService.userProfile.then(user => {
                this.username = user.username;
            });
        }
    }

    ngAfterViewInit() {
        if (window.matchMedia('prefers-color-scheme: dark')) {
            this.darkModeService.setDarkMode(true);
            this.darkModeToggle.nativeElement.setAttribute('checked', '');
        }
    }

    async navigateToLogin() {
        const url = `/login?redirect=${this.router.url}`;
        console.log(url);
        await this.router.navigateByUrl(url);
    }

    async navigateToProfile() {
        // const user = await this.authService.userProfile;
        // console.log(user);
        const url = `/profile/${this.username}`;
        await this.router.navigateByUrl(url);
    }

    toggleDarkMode() {
        const darkMode = !this.darkModeService.getDarkModeValue();
        this.darkModeService.setDarkMode(darkMode);

    }

    logout() {
        this.authService.logout();
    }
}
