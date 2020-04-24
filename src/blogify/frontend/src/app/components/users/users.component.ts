import { Component, OnInit } from '@angular/core';
import { User } from '../../models/User';
import { UserService } from "../../shared/services/user-service/user.service";

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.scss']
})
export class UsersComponent implements OnInit {

    title = 'Users';

    users: User[];

    constructor(private userService: UserService) {}

    ngOnInit() {
        this.userService
            .getAllUsers()
            .then( users => {
                this.users = users;
            });
    }

}
