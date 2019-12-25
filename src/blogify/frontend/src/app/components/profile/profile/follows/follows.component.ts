import { Component, OnInit } from '@angular/core';
import { User } from "../../../../models/User";
import { ActivatedRoute, Params } from '@angular/router';
import { UserService } from '../../../../shared/services/user-service/user.service';
import { AuthService } from '../../../../shared/auth/auth.service';

@Component({
    selector: 'app-follows',
    templateUrl: './follows.component.html',
    styleUrls: ['./follows.component.scss']
})
export class FollowsComponent implements OnInit {

    followed: User;
    following: User[];

    constructor (
        private userService: UserService,
        private route: ActivatedRoute,
        private authService: AuthService
    ) {}

    ngOnInit() {
        this.route.parent.params.subscribe( async (params: Params) => {
            this.followed = await this.authService.getByUsername(params['username']);
            this.following = await this.authService.fillUsersFromUUIDs(this.followed.followers);
        });
    }

}
