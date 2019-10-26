import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../shared/auth/auth.service';
import { CommentsService } from '../../services/comments/comments.service';
import { User } from '../../models/User';

@Component({
    selector: 'app-show-article',
    templateUrl: './show-article.component.html',
    styleUrls: ['./show-article.component.scss']
})
export class ShowArticleComponent implements OnInit {

    routeMapSubscription: Subscription;
    article: Article;
    user: User;

    constructor (
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
        public authService: AuthService,
        private commentsService: CommentsService
    ) {}

    showUpdateButton = false;
    showDeleteButton = false;

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');
            console.log(articleUUID);

            this.article = await this.articleService.getArticleByUUID (
                articleUUID,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt']
            );

            this.showUpdateButton = (await this.authService.userUUID) == this.article.createdBy.uuid;
            this.showDeleteButton = (await this.authService.userUUID) == this.article.createdBy.uuid;

            console.log(this.article);
        });
    }


    deleteArticle() {
        this.articleService.deleteArticle(this.article.uuid).then(it => console.log(it));
    }

}
