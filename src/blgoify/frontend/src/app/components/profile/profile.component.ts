import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from 'src/app/services/auth/auth.service';
import { User } from 'src/app/models/User';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {
    token: string;
    routeMapSubscribtion: Subscription;
    user: User;

    constructor(private activatedRoute: ActivatedRoute, private authService: AuthService) { }

    ngOnInit() {
        this.routeMapSubscribtion = this.activatedRoute.paramMap.subscribe(async (map) => {
            const userUUID = map.get('uuid');
            console.log(this.token)
            this.user = await this.authService.getUser(userUUID, (await this.authService.currentUserToken))
            console.log(userUUID);
        })
    }

    ngOnDestroy() {
        this.routeMapSubscribtion.unsubscribe()
    }

}
