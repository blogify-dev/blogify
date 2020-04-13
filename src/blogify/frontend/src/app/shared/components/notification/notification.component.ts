import { Component, Input, OnInit } from '@angular/core';
import { Notification } from '../../../models/Notification';

@Component({
    selector: 'app-notification',
    templateUrl: './notification.component.html',
    styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {

    @Input() notification: Notification;

    constructor() {}

    ngOnInit() {

    }

}
