import { Component, OnInit } from '@angular/core';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { UserService } from '@blogify/shared/services/user-service/user.service';
import { ActivatedRoute, Params } from '@angular/router';
import { User } from '@blogify/models/User';
import { faUser } from '@fortawesome/free-regular-svg-icons';
import { faGavel, faShieldAlt } from '@fortawesome/free-solid-svg-icons';
import { idOf } from '@blogify/models/Shadow';
import { StateService } from '@blogify/shared/services/state/state.service';

@Component({
    selector: 'b-manage',
    templateUrl: './manage.component.html',
    styleUrls: ['./manage.component.scss']
})
export class ManageComponent implements OnInit {
    
    faUser = faUser;
    faShield = faShieldAlt;

    faGavel = faGavel;

    forUser: User;
    
    constructor (
      private route: ActivatedRoute,
      private authService: AuthService,
      private userService: UserService,
      private stateService: StateService
    ) {}

    ngOnInit() {
        this.route.parent.params.subscribe((params: Params) => {
            const username = params.username;

            this.userService.getByUsername(username).then(async user => {
                return this.forUser = await this.userService.getUser(user.uuid);
            });
        });
    }
    
    toggleUserAdmin() {
        return this.userService.toggleUserAdmin(this.forUser, this.authService.currentUser.token)
            .then(_ => {
                this.forUser.isAdmin = !this.forUser.isAdmin;
                this.stateService.cacheUser(this.forUser);
            })
            .catch(e => console.error(`[blogifyUsers] Couldn't toggle user '${idOf(this.forUser)}' admin`, e));
    }

}
