import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../shared/auth/auth.service';
import { User } from '../../models/User';
import { faHeart as faHeartFilled } from '@fortawesome/free-solid-svg-icons';
import { faClipboard, faEdit, faHeart, faTrashAlt} from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from "ngx-clipboard";

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

    showUpdateButton = false;
    showDeleteButton = false;

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');

            this.article = await this.articleService.getArticleByUUID (
                articleUUID,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt', 'likeCount']
            );

            this.authService.observeIsLoggedIn().subscribe(async it => {
                this.showUpdateButton = it && (await this.authService.userUUID) == (<User> this.article.createdBy).uuid;
                this.showDeleteButton = it && (await this.authService.userUUID) == (<User> this.article.createdBy).uuid;
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
            console.error(`[blogifyArticles] Couldn't like ${this.article.uuid}` )
        })
    }

    deleteArticle() {
        this.articleService.deleteArticle(this.article.uuid).then(() => {});
        this.router.navigateByUrl("/home").then(() => {})
    }

    copyUrlToClipboard() {
        this.clipboardService.copyFromContent(window.location.href)
    }
}
