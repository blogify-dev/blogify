import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth/auth.service";
import { LoginCredentials, User } from '../../models/User';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

    loginCredentials: LoginCredentials = { username: '', password: '' };
    user: User;

    constructor(private authService: AuthService) {}

    ngOnInit() {}

    async login() {
        const token = await this.authService.login(this.loginCredentials);

        console.log(token);

        const uuid = this.authService.userUUID;
        this.user  = this.authService.userProfile;

        console.log(uuid);
        console.log(this.user);
        console.log(this.loginCredentials);
        console.log(this.authService.userToken);
    }

}
