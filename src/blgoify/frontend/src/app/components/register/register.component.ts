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

  constructor(private authService: AuthService) {
    this.ngOnInit();
  }

  ngOnInit() {
  }

  //TODO: add async methods for client display
  register(){
      console.log(this.user)
    this.authService.register(this.user).subscribe((it) => {
      this.user = it;
      console.log(this.user)
      return this.user;
    })
  }

    // async register2() {
    //     await this.authService.register(this.user).toPromise();
    //     console.log(this.user);
    //     console.log(this.user);
    // }
}
