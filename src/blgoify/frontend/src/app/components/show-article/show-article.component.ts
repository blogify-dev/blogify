import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article, Content } from "../../models/Article";
import { ArticleService } from "../../services/article/article.service";
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-show-article',
    templateUrl: './show-article.component.html',
    styleUrls: ['./show-article.component.scss']
})
export class ShowArticleComponent implements OnInit {
    routeMapSubscription: Subscription;
    article: Article;
    articleContent: Content;

    constructor(private activatedRoute: ActivatedRoute, private articleService: ArticleService) {
    }

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');
            console.log(articleUUID);
            this.article = await this.articleService.getArticleByUUID(articleUUID).toPromise();
            console.log(this.article);
            this.articleContent = await this.articleService.getArticleContent(articleUUID).toPromise();
            console.log(this.articleContent);
        })
    }

}
