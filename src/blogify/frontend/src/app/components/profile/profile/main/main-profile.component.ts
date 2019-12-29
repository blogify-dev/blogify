import {Component, OnInit} from '@angular/core';
import {Tab, TabList} from '../../../../shared/components/tab-header/tab-header.component';
import {User} from '../../../../models/User';
import {ActivatedRoute, Params} from '@angular/router';
import {AuthService} from '../../../../shared/auth/auth.service';
import {UserService} from '../../../../shared/services/user-service/user.service';
import {HttpResponse} from '@angular/common/http';

@Component({
    selector: 'app-main-profile',
    templateUrl: './main-profile.component.html',
    styleUrls: ['./main-profile.component.scss']
})
export class MainProfileComponent implements OnInit {

    user: User;

    isLoggedIn = false;
    isSelf = false;
    alreadyFollowed = false;

    baseTabs: TabList = [
        new Tab('Overview', 'overview'),
        new Tab('Friends', 'friends'),
    ];

    loggedInTabs: TabList = [
        new Tab('Settings', 'settings'),
    ];

    finalTabs: TabList = this.baseTabs;

    constructor (
        private authService: AuthService,
        private userService: UserService,
        private route: ActivatedRoute
    ) {}

    ngOnInit() {
        // This listener updates the logged in value, is necessary for tabs.
        this.authService.observeIsLoggedIn().subscribe(value => {
            this.isLoggedIn = value;
            this.updateTabs();
        });

        this.route.params.subscribe(async (params: Params) => {
            let username = params['username'];

            this.user = await this.authService.getByUsername(username);
            this.alreadyFollowed = this.user.followers.findIndex(async _ => (await this.authService.userProfile).uuid) != -1

            // This second listener must always be called at least once after user variable is initialized,
            // so it needs to be created here. We update the tabs as well.
            this.authService.observeIsLoggedIn().subscribe(async value => {
                this.isSelf = value && this.user.uuid === (await this.authService.userProfile).uuid;
                this.updateTabs();
            });
        });
    }

    /**
     * Make sure tabs are consistent for logged in or not, self or not
     */
    private updateTabs() {
        if (this.isLoggedIn && this.isSelf) {
            this.finalTabs = this.baseTabs.concat(this.loggedInTabs);
        } else {
            this.finalTabs = this.baseTabs;
        }
    }

    /**
     * Toggle the follow state and update UI accordingly
     */
    toggleFollow() {
        this.userService.toggleFollowUser(this.user, this.authService.userToken)
            .then((r: HttpResponse<Object>) => {
                if (r.status == 200) this.alreadyFollowed = !this.alreadyFollowed;
            }).catch(e => {
                console.error(`[blogifyUsers] Couldn't like ${this.user.uuid}` )
            });
    }

}
