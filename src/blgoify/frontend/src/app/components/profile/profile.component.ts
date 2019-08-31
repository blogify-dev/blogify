import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {
    routeMapSubscribtion: Subscription;
    userUUID: string;

    constructor(private activatedRoute: ActivatedRoute) { }

    ngOnInit() {
        this.routeMapSubscribtion = this.activatedRoute.paramMap.subscribe((map) => {
            this.userUUID = map.get('uuid');
            console.log(this.userUUID);
        })
    }

    ngOnDestroy() {
        this.routeMapSubscribtion.unsubscribe()
    }

}
