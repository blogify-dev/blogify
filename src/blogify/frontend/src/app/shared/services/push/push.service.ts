import { Injectable } from '@angular/core';
import { webSocket } from 'rxjs/webSocket';
import { AuthService } from '../../auth/auth.service';
import { CommentsService } from '../../../services/comments/comments.service';
import { Subject } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PushService {

    private ws = webSocket<string>({
        url: 'ws://' + document.location.hostname + '/push/connect',
        deserializer: event => event.data as string,
        serializer: data => data
    });

    private authenticated = false;

    private notificationsSubject = new Subject<EventPayload>();

    constructor(private authService: AuthService, private commentsService: CommentsService) {
        this.authService.observeIsLoggedIn().subscribe(loggedIn => {
            if (loggedIn) {
                this.ws.next(this.authService.userToken);
                this.ws.subscribe((msg) => {
                    if (!this.authenticated) {
                        if (msg.match(/AUTH OK/)) {
                            this.authenticated = true;
                        }
                    } else {
                        const parsed = JSON.parse(msg) as EventPayload;

                        if (parsed.t === 'Activity') {
                            const eventClassName = parsed.e.replace('Event', '');

                            if (eventClassName === 'CommentCreate') {
                                const data = parsed.d as CommentCreatePayload;

                                this.commentsService.registerSubmittedComment({
                                    article: data.article,
                                    commenter: data.commenter,
                                    uuid: data.comment
                                });
                            }
                        } else if (parsed.t === 'Notification') {
                            this.notificationsSubject.next(parsed);
                        }
                    }
                });
            }
        });
    }

    get events() {
        return this.notificationsSubject.asObservable();
    }
}


interface EventPayload {
    e: string;
    t: 'Activity' | 'Notification';
    d: object;
}

interface CommentCreatePayload {
    article: string;
    commenter: string;
    comment: string;
}
