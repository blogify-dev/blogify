import { Component, OnInit } from '@angular/core';
import { AuthService } from './shared/services/auth/auth.service';
import { CommentsService } from './services/comments/comments.service';
import { ArticleService } from './services/article/article.service';
import { NotificationsService } from './shared/services/notifications/notifications.service';
import { StaticContentService } from './services/static/static-content.service';
import { Router } from '@angular/router';
import { StaticFile } from './models/Static';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
    color = 'dark';

    constructor(
        public authService: AuthService,
        private commentsService: CommentsService,
        private articleService: ArticleService,
        private notificationsService: NotificationsService,
        private staticContentService: StaticContentService,
        private router: Router,
    ) {}

    async ngOnInit() {
        this.notificationsService.liveNotifications.subscribe(async (msg) => {
            const notificationIcon = msg.icon['fileId']
                ? this.staticContentService.urlFor(msg.icon as StaticFile)
                : undefined;

            new Notification(msg.header, {
                icon: notificationIcon,
                body: msg.desc,
            }).addEventListener('click', () => {
                this.router.navigateByUrl(msg.routerLink);
            });
        });
    }
}
