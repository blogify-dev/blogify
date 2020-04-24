import { Component, OnInit} from '@angular/core';
import { AuthService } from '../../shared/services/auth/auth.service';
import { Router } from '@angular/router';
import { User } from '../../models/User';
import { faBell, faMoon, faSignOutAlt } from '@fortawesome/free-solid-svg-icons';
import { ThemeService } from "../../services/theme/theme.service";

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

    user: User;

    areNotificationsShowing = false;

    faSignOutAlt = faSignOutAlt;
    faBell = faBell;
    faMoon = faMoon;

    constructor (
        public authService: AuthService,
        private router: Router,
        private themeService: ThemeService,
    ) {}

    ngOnInit() {
        this.authService.observeIsLoggedIn().subscribe(async value => {
            if (value) {
                this.user = await this.authService.currentUser;
            } else {
                this.user = undefined;
            }
        });
    }

    async navigateToLogin() {
        const url = `/login?redirect=${this.router.url}`;
        console.log(url);
        await this.router.navigateByUrl(url);
    }

    async navigateToProfile() {
        const it = await this.authService.currentUser
        const url = `/profile/${it.username}`;
        await this.router.navigateByUrl(url);

    }

    toggleDarkMode() {
        this.themeService.toggleTheme();
    }

    toggleNotifications() {
        this.areNotificationsShowing = !this.areNotificationsShowing;
    }

    logout() {
        this.authService.logout();
    }
}
