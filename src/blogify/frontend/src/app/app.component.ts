import { Component, OnInit } from '@angular/core';
import { AuthService } from './shared/auth/auth.service';
import { PushService } from './shared/services/push/push.service';
import { ToastrService } from 'ngx-toastr';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    color = 'dark';

    constructor (
        public authService: AuthService,
        public pushService: PushService,
        private toastrService: ToastrService
    ) {}


    ngOnInit(): void {
        this.pushService.events.subscribe(msg => {
            if (msg) {
                this.toastrService.show(JSON.stringify(msg.d), msg.e);
            }
        });
    }
}
