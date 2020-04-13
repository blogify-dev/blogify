import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { EventPayload } from '../../../models/Events';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../auth/auth.service';

@Injectable({
    providedIn: 'root'
})
export class NotificationsService {

    private notificationSubject = new Subject<EventPayload>();

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    registerNotificationEvent(payload: EventPayload) {
        this.notificationSubject.next(payload);
    }

    get notifications() {
        return this.notificationSubject.asObservable();
    }

    fetchMyNotifications() {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.userToken}`
            }),
        };
        return this.httpClient.get<NotificationsPayload>('/api/users/me/notifications', httpOptions).toPromise();
    }
}

interface NotificationsPayload {
    data: object;
    emitter: string;
    timestamp: number;
    name: string;
}
