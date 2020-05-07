import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../shared/services/auth/auth.service';
import { LoginCredentials, RegisterCredentials, User } from '../../models/User';
import { Router } from '@angular/router';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

    registerCredentials: RegisterCredentials = { name: '', username: '', password: '', email: '' };
    loginCredentials: LoginCredentials = { username: '', password: '' };
    keepLoggedIn = false;

    user: User;
    private redirectTo: string;

    constructor(private authService: AuthService, public router: Router) {}

    ngOnInit() {
        const redirect = (this.router.url.split(/\?redirect=/)[1]).replace(/%2f/ig, '/');
        if (redirect) {
            this.redirectTo = redirect;
        }
    }

    async login() {
        this.authService.login(this.loginCredentials, this.keepLoggedIn)
            .then(async () => {
                this.user = await this.authService.currentUser;

                if (this.redirectTo) {
                    await this.router.navigateByUrl(this.redirectTo);
                } else {
                    await this.router.navigateByUrl('/home');
                }
            })
            .catch((error) => {
                alert("An error occurred during login");
                console.error(`[login]: ${error}`)
            });
    }

    async register() {
        this.authService.register(this.registerCredentials)
            .then(async user => {
                this.user = user;

                if (this.redirectTo) {
                    await this.router.navigateByUrl(this.redirectTo);
                } else {
                    await this.router.navigateByUrl('/home');
                }
            })
            .catch((error) => {
                alert("An error occurred during login");
                console.error(`[register]: ${error}`)
            });
    }


}
