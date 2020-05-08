import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { User } from 'src/app/models/User';
import { UserService } from '@blogify/shared/services/user-service/user.service';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {

    routeMapSubscription: Subscription;
    user: User;

    constructor (
        private activatedRoute: ActivatedRoute,
        private userService: UserService,
    ) {}

    async ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async map => {
            const username = map.get('username');

            this.user = await this.userService.getByUsername(username);
        });
    }

    ngOnDestroy() {
        this.routeMapSubscription.unsubscribe();
    }

}
