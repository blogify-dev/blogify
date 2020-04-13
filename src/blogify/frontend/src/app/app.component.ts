import { Component, OnInit } from '@angular/core';
import { AuthService } from './shared/auth/auth.service';
import { PushService } from './shared/services/push/push.service';
import { ToastrService } from 'ngx-toastr';
import { NotificationComponent } from './shared/components/notification/notification.component';
import { Notification } from './models/Notification';
import { CommentReplyPayload, CommentsService } from './services/comments/comments.service';
import { idOf } from './models/Shadow';
import { ArticleService } from './services/article/article.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
    color = 'dark';

    constructor (
        public authService: AuthService,
        public pushService: PushService,
        private commentsService: CommentsService,
        private articleService: ArticleService,
        private toastrService: ToastrService
    ) {}


    async ngOnInit() {
        this.pushService.events.subscribe(async msg => {
            if (msg) {
                if (msg.t === 'Notification') {
                    const data = msg.d;
                    let notification: Notification;

                    if (msg.e === 'CommentReplyEvent') {
                        const payload = data as CommentReplyPayload;

                        const newComment = await this.commentsService.getCommentByUUID(payload.newComment);
                        const author = await this.authService.fetchUser(idOf(newComment.commenter));

                        notification = {
                            icon: null,
                            header: `${author.username} responded to your comment`,
                            desc: `"${newComment.content.substr(0, 25)}"`,
                            routerLink: '/users'
                        };
                    }

                    (this.toastrService.show()
                        .toastRef.componentInstance as NotificationComponent).notification = notification;
                }
            }
        });
    }
}
