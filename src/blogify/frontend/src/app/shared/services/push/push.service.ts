import {Injectable} from '@angular/core';
import {webSocket} from 'rxjs/webSocket';
import {AuthService} from '../../auth/auth.service';
import {CommentsService} from '../../../services/comments/comments.service';
import {BehaviorSubject} from 'rxjs';

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

    private eventsBehaviorSubject = new BehaviorSubject<EventPayload>(undefined);

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
                        const parsed = JSON.parse(msg);
                        if (parsed.e.endsWith('Event')) {
                            const event = parsed.e.replace('Event', '');
                            this.eventsBehaviorSubject.next(parsed);
                            switch (event) {
                                // TODO: Implement here
                                case 'CommentReply':
                                    // console.log('Comment reply added', parsed.d);
                                    break;
                                default:
                                    break;
                            }
                        } else if (parsed.e.startsWith('Activity')) {
                            const on = parsed.e.replace('Activity', '');
                            switch (on.toLowerCase()) {
                                case 'comment':
                                    const data = parsed.d;
                                    this.commentsService.submitNewComment({
                                        article: data.article,
                                        commenter: data.commenter,
                                        uuid: data.uuid
                                    });
                            }
                        }
                    }
                });
            }
        });
    }

    get events() {
        return this.eventsBehaviorSubject.asObservable();
    }
}


interface EventPayload {
    e: string;
    d: object;
}
