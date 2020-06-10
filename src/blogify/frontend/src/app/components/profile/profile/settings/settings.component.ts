import { Component, OnInit } from '@angular/core';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import {faUpload} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

    faUpload = faUpload;

    pfpFile: File = null;
    coverFile: File = null;

    notificationsAreEnabled = !(Notification.permission === 'default' || Notification.permission === 'denied');

    constructor (
        public authService: AuthService,
    ) {}

    ngOnInit() {}

    async pfpFileChange(event) {
        this.pfpFile = event.target.files[0];
    }

    async coverFileChange(event) {
        this.coverFile = event.target.files[0];
    }

    async setProfilePicture() {
        await this.authService.uploadFile(this.pfpFile, 'profilePicture');
    }

    async setCoverPicture() {
        await this.authService.uploadFile(this.coverFile, 'coverPicture');
    }

    async requestNotificationsPermission() {
        if (Notification.permission !== 'granted') {
            await Notification.requestPermission();
            this.notificationsAreEnabled = !(Notification.permission === 'default' || Notification.permission === 'denied');
        }
    }
}
