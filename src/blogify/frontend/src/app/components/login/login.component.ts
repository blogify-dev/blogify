import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../shared/auth/auth.service';
import { LoginCredentials, RegisterCredentials, User } from '../../models/User';
import { Router} from '@angular/router';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

    registerCredentials: RegisterCredentials = { name: '', username: '', password: '', email: '' };
    loginCredentials: LoginCredentials = { username: '', password: '' };

    user: User;
    private redirectTo: string;

    constructor(private authService: AuthService, public router: Router) {}

    ngOnInit() {
        const redirect = (this.router.url.split(/\?redirect=/)[1]).replace(/%2f/ig, '/');
        if (redirect) {
            this.redirectTo = redirect;
        }
        console.log(this.redirectTo);
    }

    async login() {
        this.authService.login(this.loginCredentials)
            .then(async token => {
                const uuid = await this.authService.userUUID;
                this.user = await this.authService.userProfile;

                // console.log('LOGIN ->');
                // console.log(uuid);
                // console.log(this.user);
                // console.log(this.loginCredentials);
                // console.log(this.authService.userToken);
                // console.log(this.redirectTo);

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

                // console.log('REGISTER ->');
                // console.log(this.user);
                // console.log(this.registerCredentials);

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
