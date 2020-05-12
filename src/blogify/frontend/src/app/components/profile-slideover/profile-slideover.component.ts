import { Component, OnInit } from '@angular/core';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { User } from '@blogify/models/User';
import { faList, faSignOutAlt } from '@fortawesome/free-solid-svg-icons';
import { faUser } from '@fortawesome/free-regular-svg-icons';

@Component({
    selector: 'app-profile-slideover',
    templateUrl: './profile-slideover.component.html',
    styleUrls: ['./profile-slideover.component.scss']
})
export class ProfileSlideoverComponent implements OnInit {

    user: User;

    faList = faList;
    faUser = faUser;

    faSignOutAlt = faSignOutAlt;

    constructor (
        public authService: AuthService
    ) {}

    ngOnInit() {
        this.user = this.authService.currentUser;
    }

}
