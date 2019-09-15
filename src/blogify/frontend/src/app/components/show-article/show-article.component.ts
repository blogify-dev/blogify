import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article, Content } from "../../models/Article";
import { ArticleService } from "../../services/article/article.service";
import { Subscription } from 'rxjs';
import { User } from "../../models/User";
import { AuthService } from "../../services/auth/auth.service";

@Component({
    selector: 'app-show-article',
    templateUrl: './show-article.component.html',
    styleUrls: ['./show-article.component.scss']
})
export class ShowArticleComponent implements OnInit {
    routeMapSubscription: Subscription;
    article: Article;
    articleContent: Content;
    articleAuthor: User;

    constructor(
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
        private authService: AuthService
    ) {
    }

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');
            console.log(articleUUID);

            this.article = await this.articleService.getArticleByUUID(articleUUID).toPromise();
            console.log(this.article);

            this.articleContent = await this.articleService.getArticleContent(articleUUID).toPromise();
            console.log(this.articleContent);

            this.articleAuthor = await this.authService.getUser(this.article.createdBy.toString());
            console.log(this.articleAuthor.username);
        })
    }

    convertTimeStampToHumanDate(time: number): string {
        return new Date(time).toDateString()
    }

}
