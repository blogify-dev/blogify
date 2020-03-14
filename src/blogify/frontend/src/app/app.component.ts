import { Component } from '@angular/core';
import {AuthService} from './shared/auth/auth.service';
import { PushService } from './shared/services/push/push.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent {
    color = 'dark';

    constructor (
        public authService: AuthService,
        public pushService: PushService
    ) {}

}
