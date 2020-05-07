import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../shared/services/auth/auth.service';
import { faHeart as faHeartFilled, faThumbtack } from '@fortawesome/free-solid-svg-icons';
import { faClipboard, faEdit, faHeart, faTrashAlt } from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from 'ngx-clipboard';
import { idOf } from '../../models/Shadow';
import {UserService} from "../../shared/services/user-service/user.service";

@Component({
    selector: 'app-show-article',
    templateUrl: './show-article.component.html',
    styleUrls: ['./show-article.component.scss']
})
export class ShowArticleComponent implements OnInit {

    routeMapSubscription: Subscription;
    article: Article;

    constructor (
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
        public authService: AuthService,
        private userService: UserService,
        private router: Router,
        private clipboardService: ClipboardService,
    ) {}

    loggedInObs = this.authService.observeIsLoggedIn();

    faHeartOutline = faHeart;
    faHeartFilled = faHeartFilled;

    faEdit = faEdit;
    faTimes = faTrashAlt;
    faCopy = faClipboard;
    faThumbtack = faThumbtack;

    isLoggedInUsersArticle = false;
    isAdmin = false;

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');

            this.article = await this.articleService.getArticle(articleUUID);

            this.authService.observeIsLoggedIn().subscribe(async state => {
                if (state) {
                    this.isLoggedInUsersArticle = idOf(this.article.createdBy) === this.authService.currentUser.uuid;
                    this.isAdmin = this.authService.currentUser.isAdmin;
                }
            });
        });
    }

    toggleLike() {
        this.articleService.likeArticle(this.article)
            .then(() => {
                this.article.likedByUser = !this.article.likedByUser;
                this.article.likeCount += (this.article.likedByUser ? 1 : -1);
            }).catch(() => console.error(`[blogifyArticles] Couldn't like ${this.article.uuid}`));
    }

    togglePin() {
        this.articleService.pinArticle(this.article.uuid)
            .then(_ => this.article.isPinned = !this.article.isPinned)
            .catch(_ => console.error(`[blogifyArticles] could not pin ${this.article.uuid}`));
    }

    deleteArticle() {
        this.articleService.deleteArticle(this.article.uuid)
            .then(_ => this.router.navigateByUrl('/home'))
            .catch(_ => console.error(`[blogifyArticles] could not delete ${this.article.uuid}\``));
    }

    copyUrlToClipboard() {
        this.clipboardService.copyFromContent(window.location.href);
    }
}
