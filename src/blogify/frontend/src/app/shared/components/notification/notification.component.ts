import { Component, Input, OnInit } from '@angular/core';
import { Notification } from '../../../models/Notification';
import { StaticFile } from '../../../models/Static';
import { IconDefinition } from '@fortawesome/fontawesome-svg-core';

@Component({
    selector: 'app-notification',
    templateUrl: './notification.component.html',
    styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {

    @Input() notification: Notification;

    // @ts-ignore
    get iconIsPfp()  { return this.notification.icon.contentType !== undefined;}
    get iconIsFa()   { return !this.iconIsPfp && this.notification !== null; }
    get iconIsNone() { return !this.iconIsPfp && !this.iconIsFa; }

    constructor() {}

    ngOnInit() {

    }

}
