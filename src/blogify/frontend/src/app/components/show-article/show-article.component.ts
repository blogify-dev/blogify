import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../shared/auth/auth.service';
import { User } from '../../models/User';
import { faHeart as faHeartFilled, faMapPin as faPin, faThumbtack} from '@fortawesome/free-solid-svg-icons';
import { faClipboard, faEdit, faHeart, faTrashAlt } from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from 'ngx-clipboard';
import { idOf } from '../../models/Shadow';

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

            this.article = await this.articleService.getArticleByUUID (
                articleUUID,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'isPinned', 'categories', 'createdAt', 'likeCount']
            );

            this.authService.observeIsLoggedIn().subscribe(async state => {
                if (state) {
                    this.isLoggedInUsersArticle = idOf(this.article.createdBy) === await this.authService.userUUID;
                    this.isAdmin = (await this.authService.fetchUser(idOf(this.article.createdBy))).isAdmin;
                }
            });
        });
    }

    toggleLike() {
        this.articleService
            .likeArticle(this.article, this.authService.userToken)
            .then(() => {
                this.article.likedByUser = !this.article.likedByUser;
                this.article.likeCount += (this.article.likedByUser ? 1 : -1);
            }).catch(() => {
            console.error(`[blogifyArticles] Couldn't like ${this.article.uuid}` );
        });
    }

    togglePin() {
        this.articleService.pinArticle(this.article.uuid)
            .then(_ => this.article.isPinned = !this.article.isPinned)
            .catch(_ => console.error(`[blogifyArticles] could not pin ${this.article.uuid}`));
    }

    deleteArticle() {
        this.articleService.deleteArticle(this.article.uuid).then(() => {});
        this.router.navigateByUrl('/home').then(() => {});
    }

    copyUrlToClipboard() {
        this.clipboardService.copyFromContent(window.location.href);
    }
}
