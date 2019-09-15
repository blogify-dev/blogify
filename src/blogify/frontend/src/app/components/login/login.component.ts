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

    constructor(private authService: AuthService) {
        this.ngOnInit();
    }

    ngOnInit() {
    }

    async login() {
        const token = await this.authService.login(this.loginCredentials);
        console.log(token);
        const uuid = await this.authService.getUserUUID(token.token);
        this.user = await this.authService.getUser(uuid.uuid);
        console.log(this.user);
        console.log(this.loginCredentials);
        console.log(this.authService.userToken);
    }

}
