import { Component, OnInit, ViewChild } from '@angular/core';
import { AuthService } from './shared/auth/auth.service';
import { NotificationComponent } from './shared/components/notification/notification.component';
import { Notification } from './models/Notification';
import { ArticleCommentReplyPayload, CommentReplyPayload, CommentsService } from './services/comments/comments.service';
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

        this.notificationsService.liveNotifications.subscribe(async msg => {
            const toastRef = this.toastrService.show().toastRef;
            const componentInstance = toastRef.componentInstance as NotificationComponent;

            componentInstance.toastRef = toastRef;
            componentInstance.notification = msg;
        });
    }
}
