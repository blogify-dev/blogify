import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {AuthService} from '../../shared/auth/auth.service';
import {Router} from '@angular/router';
import {DarkModeService} from '../../services/darkmode/dark-mode.service';
import {User} from '../../models/User';
import { faBell, faMoon, faSignOutAlt } from '@fortawesome/free-solid-svg-icons';


@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, AfterViewInit {

    @ViewChild('darkModeToggle', {static: false, read: ElementRef}) darkModeToggle: ElementRef;

    user: User;

    faSignOutAlt = faSignOutAlt;
    faBell = faBell;
    faMoon = faMoon;

    constructor (
        public authService: AuthService,
        private router: Router,
        private darkModeService: DarkModeService,
    ) {}

    async ngOnInit() {
        this.user = await this.authService.userProfile;
        console.log(this.user.profilePicture);
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
        await this.authService.userProfile.then(it => {
            const url = `/profile/${it.username}`;
            this.router.navigateByUrl(url);
        });
    }

    toggleDarkMode() {
        const darkMode = !this.darkModeService.getDarkModeValue();
        this.darkModeService.setDarkMode(darkMode);

    }

    logout() {
        this.authService.logout();
    }
}
