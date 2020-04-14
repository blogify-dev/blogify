import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthService } from './shared/auth/auth.service';
import { NotificationComponent } from './shared/components/notification/notification.component';
import { Notification } from './models/Notification';
import { CommentReplyPayload, CommentsService } from './services/comments/comments.service';
import { idOf } from './models/Shadow';
import { ArticleService } from './services/article/article.service';
import { NotificationsService } from './shared/services/notifications/notifications.service';
import { ToastContainerDirective, ToastrService } from 'ngx-toastr';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    color = 'dark';

    constructor (
        public authService: AuthService,
        private commentsService: CommentsService,
        private articleService: ArticleService,
        private toastrService: ToastrService,
        private notificationsService: NotificationsService,
    ) {}

    @ViewChild(ToastContainerDirective, {static: true})
    toastContainer: ToastContainerDirective;

    async ngOnInit() {
        this.toastrService.overlayContainer = this.toastContainer;
        this.notificationsService.notifications.subscribe(async msg => {
            if (msg) {
                const data = msg.d;
                let notification: Notification;

                if (msg.e === 'CommentReplyEvent') {
                    const payload = data as CommentReplyPayload;

                    const newComment = await this.commentsService.getCommentByUUID(payload.newComment);
                    const author = await this.authService.fetchUser(idOf(newComment.commenter));

                    notification = {
                        icon: author.profilePicture,
                        header: `${author.username} responded to your comment`,
                        desc: `"${newComment.content.substr(0, 25)}"`,
                        routerLink: '/users'
                    };
                }

                (this.toastrService.show()
                    .toastRef.componentInstance as NotificationComponent).notification = notification;
                }
        });
    }
}
