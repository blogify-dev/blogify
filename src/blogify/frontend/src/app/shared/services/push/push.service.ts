import { Injectable } from '@angular/core';
import { webSocket } from 'rxjs/webSocket';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { CommentsService } from '@blogify/core/services/comments/comments.service';
import { CommentCreatePayload, EventPayload } from '@blogify/models/Events';
import { NotificationsService } from '@blogify/shared/services/notifications/notifications.service';
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
            timer(3_500).subscribe(_ => {
                if (loggedIn && !this.authenticated) alert('Could not open a streaming channel. Please report the issue to a project maintainer.');
            });

            if (loggedIn) {
                console.log('[blogifyStreaming] Trying to authenticate to server ...');
                this.ws.next(this.authService.currentUser.token);

                this.ws.subscribe(msg => {
                    if (!this.authenticated) {
                        if (msg.match(/AUTH OK/)) {
                            this.authenticated = true;

                            console.log('[blogifyStreaming] Successfully authenticated to server');
                        }
                    } else {
                        const parsed = JSON.parse(msg) as EventPayload;

                        if (parsed.t === 'Activity') {
                            if (parsed.e === 'blogify.backend.resources.Comment.CommentCreateEvent') {
                                const data = parsed.d as CommentCreatePayload;

                                this.commentsService.registerCommentFromServer({
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
    }
}



