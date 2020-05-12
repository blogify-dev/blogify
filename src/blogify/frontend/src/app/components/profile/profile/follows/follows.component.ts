import { Component, OnInit } from '@angular/core';
import { User } from '@blogify/models/User';
import { ActivatedRoute, Params } from '@angular/router';
import { UserService } from '@blogify/shared/services/user-service/user.service';

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
    ) {}

    ngOnInit() {
        this.route.parent.params.subscribe( async (params: Params) => {
            this.followed = await this.userService.getByUsername(params['username']);
            this.following = await this.userService.fillUsersFromUUIDs(this.followed.followers);
        });
    }

}
