import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../shared/auth/auth.service';
import { User } from '../../models/User';
import { faPenFancy, faTimes, faShare } from '@fortawesome/free-solid-svg-icons';
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

    faEdit = faPenFancy;
    faTimes = faTimes;
    faShare = faShare;

    showUpdateButton = false;
    showDeleteButton = false;

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');

            this.article = await this.articleService.getArticleByUUID (
                articleUUID,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt']
            );

            this.authService.observeIsLoggedIn().subscribe(async it => {
                this.showUpdateButton = it && (await this.authService.userUUID) == (<User> this.article.createdBy).uuid;
                this.showDeleteButton = it && (await this.authService.userUUID) == (<User> this.article.createdBy).uuid;
            });
        });
    }


    deleteArticle() {
        this.articleService.deleteArticle(this.article.uuid).then(it => console.log(it));
        this.router.navigateByUrl("/home").then(() => {})
    }

    copyUrlToClipboard() {
        this.clipboardService.copyFromContent(window.location.href)
    }
}
