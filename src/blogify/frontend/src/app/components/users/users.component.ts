import { Component, OnInit } from '@angular/core';
import { User } from '@blogify/models/User';
import { UserService } from '@blogify/shared/services/user-service/user.service';

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
