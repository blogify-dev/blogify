import { Component, OnInit } from '@angular/core';
import { User } from '../../models/User';
import { AuthService } from '../../shared/auth/auth.service';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

    title = 'Users';

    users: User[];

    constructor(private authService: AuthService) {}

    ngOnInit() {
        this.authService
            .getAllUsers()
            .then( users => {
                this.users = users;
            });
    }

}
