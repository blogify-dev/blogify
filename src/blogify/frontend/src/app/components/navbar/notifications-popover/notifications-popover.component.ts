import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { NotificationsService } from '../../../shared/services/notifications/notifications.service';
import { Notification } from '../../../models/Notification';
import { faBan } from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'b-notifications-popover',
    templateUrl: './notifications-popover.component.html',
    styleUrls: ['./notifications-popover.component.scss']
})
export class NotificationsPopoverComponent implements OnInit {

    @Output() needsToClose = new EventEmitter<any>();

    constructor (
        private notificationsService: NotificationsService
    ) {}

    faBan = faBan;

    notifications: Notification[];

    ngOnInit() {
        // Get old notifications

        this.notificationsService.fetchMyNotifications()
            .then(notifs => this.notifications = notifs)
            .catch(err => console.error('[blogifyNotifications] could not fetch all notification data'));

        // Subscribe to new notifications

        this.notificationsService.liveNotifications
            .subscribe(notif => this.notifications.unshift(notif));
    }

    removeNotif(notif: Notification) {
        this.notifications.splice(this.notifications.indexOf(notif), 1);

        if (this.notifications.length < 1)
            this.needsToClose.emit();
    }

}
