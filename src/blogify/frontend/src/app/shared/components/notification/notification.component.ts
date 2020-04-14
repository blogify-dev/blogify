import { Component, OnInit } from '@angular/core';
import { Notification } from '../../../models/Notification';
import { ToastRef } from 'ngx-toastr';
import { faChevronDown } from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'app-notification',
    templateUrl: './notification.component.html',
    styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {

    toastRef: ToastRef<any>;

    notification: Notification;

    faChevronDown = faChevronDown;

    // @ts-ignore
    get iconIsPfp()  { return this.notification.icon.contentType !== undefined;}
    get iconIsFa()   { return !this.iconIsPfp && this.notification !== null; }
    get iconIsNone() { return !this.iconIsPfp && !this.iconIsFa; }

    constructor() {}

    ngOnInit() {}

}
