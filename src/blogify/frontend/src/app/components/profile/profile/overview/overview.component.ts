import { Component, OnInit } from '@angular/core';
import { Article } from "../../../../models/Article";
import { ArticleService } from "../../../../services/article/article.service";
import { ActivatedRoute, Params, Router } from '@angular/router';

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {

    articles: Article[] = [];

    constructor (
        private articleService: ArticleService,
        private route: ActivatedRoute
    ) {}

    ngOnInit() {
        this.route.parent.params.subscribe((params: Params) => {
            let username = params['username'];

            this.articleService.getArticleByForUser (
                username,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt']
            ).then(articles => {
                this.articles = articles
            })
        })
    }

}
