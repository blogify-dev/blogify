import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {AuthService} from "../../../../shared/auth/auth.service";

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

    file: File = null;

    constructor (
        private authService: AuthService,
    ) {}

    ngOnInit() {}

    async fileChange(event) {
        this.file = event.target.files[0];
    }

    async setProfilePicture() {
        await this.authService.addProfilePicture(this.file, (await this.authService.userUUID))
    }

}
