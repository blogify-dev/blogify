import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth/auth.service";
import { UsernamePasswordCredentials } from '../../models/User';
import {Observable} from "rxjs";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  user: UsernamePasswordCredentials;

  constructor(private authService: AuthService) {
    this.ngOnInit();
  }

  ngOnInit() {
  }

  login(){
    this.authService.login(this.user).subscribe((it: any) => {
      this.user = it;
      console.log(this.user)
    })
  }

}
