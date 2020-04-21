import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { EventPayload } from '../../../models/Events';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { Notification } from '../../../models/Notification';
import { ArticleCommentReplyPayload, CommentReplyPayload, CommentsService } from '../../../services/comments/comments.service';
import { idOf } from '../../../models/Shadow';
import { ArticleService } from '../../../services/article/article.service';
import { UserService } from '../user-service/user.service';

@Injectable({
    providedIn: 'root'
})
export class NotificationsService {

    constructor (
        private httpClient: HttpClient,
        private authService: AuthService,
        private userService: UserService,
        private articleService: ArticleService,
        private commentsService: CommentsService
    ) {}

    private notificationSubject = new Subject<Notification>();

    get liveNotifications() {
        return this.notificationSubject.asObservable();
    }

    private async convertPayloadToNotification(eventPayload: EventPayload): Promise<Notification> {
        const eventClasses = {
            COMMENT_REPLY: 'blogify.backend.resources.Comment.CommentReplyEvent',
            ARTICLE_REPLY: 'blogify.backend.resources.Article.CommentReplyEvent',
        };

        if (eventPayload) {
            const data = eventPayload.d;
            let notification: Notification;

            if (eventPayload.e === eventClasses.COMMENT_REPLY) {
                const payload = data as CommentReplyPayload;

                const newComment = await this.commentsService.getCommentByUUID(payload.newComment);
                const author = await this.userService.getUser(idOf(newComment.commenter));

                notification = {
                    icon: author.profilePicture,
                    header: `${author.username} responded to your comment`,
                    desc: `"${newComment.content.substr(0, 25)}..."`,
                    routerLink: `/article/${idOf(newComment.article)}`
                };
            } else if (eventPayload.e === eventClasses.ARTICLE_REPLY) {
                const payload = data as ArticleCommentReplyPayload;

                const newComment = await this.commentsService.getCommentByUUID(payload.newComment);
                const author = await this.userService.getUser(idOf(newComment.commenter));
                const article = await this.articleService.getArticle(payload.onArticle);

                notification = {
                    icon: author.profilePicture,
                    header: `${author.username} commented on "${article.title.substr(0, 15)}"...`,
                    desc: `"${newComment.content.substr(0, 25)}"`,
                    routerLink: `/article/${idOf(newComment.article)}`
                };
            }

            return notification;
        }
    }

    async registerNotificationPayload(payload: EventPayload) {
        this.notificationSubject.next(await this.convertPayloadToNotification(payload));
    }

    async fetchMyNotifications(): Promise<Notification[]> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            }),
        };

        return this.httpClient.get<NotificationsPayload[]>('/api/users/me/notifications', httpOptions).toPromise()
            .then(payloads => Promise.all ( payloads.map ( async p =>
                this.convertPayloadToNotification({ e: p.klass, d: p.data, t: 'Notification' })
            ))).then(notifs => notifs.filter(n => n));
    }
}

interface NotificationsPayload {
    data: object;
    emitter: string;
    timestamp: number;
    klass: string;
}
