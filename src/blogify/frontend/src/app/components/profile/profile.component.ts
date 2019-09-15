import { Component, OnInit, OnDestroy } from '@angular/core';
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
    routeMapSubscription: Subscription;
    user: User;

    constructor(private activatedRoute: ActivatedRoute, private authService: AuthService) {
    }

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const userUUID = map.get('uuid');
            this.user = await this.authService.getUser(userUUID);
            console.log(userUUID);
            console.log(this.user)
        })
    }

    ngOnDestroy() {
        this.routeMapSubscription.unsubscribe()
    }

}
