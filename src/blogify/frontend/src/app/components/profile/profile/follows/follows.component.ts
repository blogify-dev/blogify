import { Component, OnInit } from '@angular/core';
import { User } from "../../../../models/User";
import { UserService } from "../../../../shared/services/user-service/user.service";
import { ActivatedRoute, Params } from "@angular/router";
import { AuthService } from "../../../../shared/auth/auth.service";

@Component({
    selector: 'app-follows',
    templateUrl: './follows.component.html',
    styleUrls: ['./follows.component.scss']
})
export class FollowsComponent implements OnInit {
    follower: User;
    follows: User[];

    constructor(
        private userService: UserService,
        private route: ActivatedRoute,
        private authService: AuthService
    ) { }

    ngOnInit() {
        this.route.parent.params.subscribe( async (params: Params) => {
            this.follower = await this.authService.getByUsername(params['username']);
            this.follows = await this.userService.follows(this.follower.uuid)
        });
    }

}
