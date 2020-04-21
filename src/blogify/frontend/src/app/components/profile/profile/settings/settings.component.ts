import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../../../shared/services/auth/auth.service";

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

    pfpFile: File = null;
    coverFile: File = null;

    constructor (
        private authService: AuthService,
    ) {}

    ngOnInit() {}

    async pfpFileChange(event) {
        this.pfpFile = event.target.files[0];
    }

    async coverFileChange(event) {
        this.coverFile = event.target.files[0];
    }

    async setProfilePicture() {
        await this.authService.uploadFile(this.pfpFile, 'profilePicture')
    }

    async setCoverPicture() {
        await this.authService.uploadFile(this.coverFile, 'coverPicture')
    }

}
