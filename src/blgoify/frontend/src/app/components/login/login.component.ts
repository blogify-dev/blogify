import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth/auth.service";
import { LoginCredentials } from '../../models/User';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

    user: LoginCredentials = { username: '', password: '' };

    constructor(private authService: AuthService) {
        this.ngOnInit();
    }

    ngOnInit() {
    }

    async login() {
        const token = await this.authService.login(this.user).toPromise();
        console.log(token);
        const uuid = await this.authService.getUserUUID(token).toPromise();
        console.log(this.authService.getUser(uuid.uuid));
        console.log(this.user)
    }

}
