import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth/auth.service";
import {User} from "../../models/User";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  user: User = {/*uuid: '',*/ password: '', /*info: '',*/ username: ''};

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
}
