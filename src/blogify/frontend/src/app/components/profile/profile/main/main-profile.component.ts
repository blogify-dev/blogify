import {Component, OnInit} from '@angular/core';
import {Tab, TabList} from '../../../../shared/components/tab-header/tab-header.component';
import {User} from '../../../../models/User';
import {ActivatedRoute, Params} from '@angular/router';
import {AuthService} from '../../../../shared/auth/auth.service';

@Component({
    selector: 'app-main-profile',
    templateUrl: './main-profile.component.html',
    styleUrls: ['./main-profile.component.scss']
})
export class MainProfileComponent implements OnInit {

    user: User;

    tabs: TabList = [
        new Tab('Overview', 'overview'),
        new Tab('Settings', 'settings')
    ];

    constructor(private authService: AuthService, private route: ActivatedRoute) {}

    ngOnInit() {
        this.route.params.subscribe(async (params: Params) => {
            let username = params['username'];

            this.user = await this.authService.getByUsername(username);
        })
    }

}
