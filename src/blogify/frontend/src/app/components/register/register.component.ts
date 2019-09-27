import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth/auth.service";
import { RegisterCredentials } from "../../models/User";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

    user: RegisterCredentials = { name: '', username: '', password: '', email: '' };


    constructor(private authService: AuthService) {}

    ngOnInit() {}

    //TODO: add async methods for client display


    // async register2() {
    //     await this.authService.register(this.user).toPromise();
    //     console.log(this.user);
    //     console.log(this.user);
    // }
}
