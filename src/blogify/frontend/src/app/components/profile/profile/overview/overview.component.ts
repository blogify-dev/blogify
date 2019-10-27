import { Component, OnInit } from '@angular/core';
import { Article } from "../../../../models/Article";
import { ArticleService } from "../../../../services/article/article.service";
import { ActivatedRoute } from "@angular/router";
import {map} from "rxjs/operators";

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {

    articles: Article[] = [];

    constructor(
        private articleService: ArticleService,
        private activatedRoute: ActivatedRoute,
    ) {}

    ngOnInit() {
        this.activatedRoute.data.pipe(
            map(it => it["username"])
        ).subscribe(username => {
            this.articleService.getArticleByForUser(
                username,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt']
            ).then(articles => {
                this.articles = articles
            })
        })
    }

}
