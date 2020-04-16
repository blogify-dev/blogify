import { Injectable } from '@angular/core';
import { webSocket } from 'rxjs/webSocket';
import { AuthService } from '../../auth/auth.service';
import { CommentsService } from '../../../services/comments/comments.service';
import { CommentCreatePayload, EventPayload } from '../../../models/Events';
import { NotificationsService } from '../notifications/notifications.service';
import { timer } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class PushService {

    private ws = webSocket<string>({
        url: (document.location.protocol === 'https:' ? 'wss://' : 'ws://') + document.location.hostname + '/push/connect',
        deserializer: event => event.data as string,
        serializer: data => data
    });

    private authenticated = false;

    constructor (
        private authService: AuthService,
        private commentsService: CommentsService,
        private notificationsService: NotificationsService
    ) {
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
                            if (parsed.e === 'blogify.backend.resources.Comment.CommentCreateEvent') {
                                const data = parsed.d as CommentCreatePayload;

                                this.commentsService.registerSubmittedComment({
                                    article: data.article,
                                    commenter: data.commenter,
                                    uuid: data.comment
                                }).then();
                            }
                        } else if (parsed.t === 'Notification') {
                            this.notificationsService.registerNotificationPayload(parsed);
                        }
                    }
                });
            }
        });

        timer(3_500).subscribe(_ => {
            if (!this.authenticated) alert('Could not open a streaming channel. Please report the issue to a project maintainer.');
        });
    }
}



