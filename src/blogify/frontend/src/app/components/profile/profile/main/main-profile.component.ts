import { Component, OnInit } from '@angular/core';
import { Tab, TabList } from '../../../../shared/components/tab-header/tab-header.component';
import { User } from '../../../../models/User';
import { ActivatedRoute, Params } from '@angular/router';
import { AuthService } from '../../../../shared/services/auth/auth.service';
import { UserService } from '../../../../shared/services/user-service/user.service';
import { HttpResponse } from '@angular/common/http';
import { faCheck, faPencilAlt, faTimes, faUserMinus, faUserPlus } from '@fortawesome/free-solid-svg-icons';

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

    faUserPlus = faUserPlus;
    faUserMinus = faUserMinus;

    faPencilAlt = faPencilAlt;
    faCheck = faCheck;
    faTimes = faTimes;

    editingBiography = false;

    baseTabs: TabList = [
        new Tab('Overview', 'overview'),
        new Tab('Friends', 'friends'),
    ];

    loggedInTabs: TabList = [
        new Tab('Settings', 'settings'),
    ];

    adminTabs: TabList = [
        new Tab('Manage', 'manage')
    ]

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
            const username = params.username;

            // Set the correct profile data
            this.user = await this.userService.getByUsername(username);

            this.authService.observeIsLoggedIn().subscribe(async loggedIn => {
                if (!loggedIn) return;

                this.alreadyFollowed
                    = this.user.followers.findIndex(it => it === this.authService.currentUser.uuid) !== -1;

            });

            // This second listener must always be called at least once after user variable is initialized,
            // so it needs to be created here. We update the tabs as well.
            this.authService.observeIsLoggedIn().subscribe(async value => {
                this.isSelf = value && this.user.uuid === this.authService.currentUser.uuid;
                this.updateTabs();
            });
        });
    }

    /**
     * Make sure tabs are consistent for logged in or not, self or not
     */
    private updateTabs() {
        this.finalTabs = [...this.baseTabs];

        if (this.isLoggedIn && this.isSelf) {
            this.finalTabs.push(...this.loggedInTabs);
        }

        if (this.isLoggedIn && this.authService.currentUser.isAdmin) {
            this.finalTabs.push(...this.adminTabs);
        }
    }

    /**
     * Toggle the follow state and update UI accordingly
     */
    toggleFollow() {
        this.userService.toggleFollowUser(this.user, this.authService.currentUser.token)
            .then((r: HttpResponse<object>) => {
                if (r.status === 200) this.alreadyFollowed = !this.alreadyFollowed;
            }).catch(e => {
                console.error(`[blogifyUsers] Couldn't like ${this.user.uuid}`, e);
            });
    }

    toggleEditingBiography = () => this.editingBiography =! this.editingBiography;

    updateBiography() {
        const text = this.user.biography;

        this.userService.updateUser(this.user, { biography: text }, this.authService.currentUser.token)
            .then(_ => this.toggleEditingBiography())
            .catch(_ => console.log('[blogifyUsers] couldn\'t update biography'));

        this.user.biography = text;
    }

}
