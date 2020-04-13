import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { EventPayload } from '../../../models/Events';

@Injectable({
    providedIn: 'root'
})
export class NotificationsService {

    private notificationSubject = new Subject<EventPayload>();

    constructor() {}

    registerNotificationEvent(payload: EventPayload) {
        this.notificationSubject.next(payload);
    }

    get notifications() {
        return this.notificationSubject.asObservable();
    }
}
