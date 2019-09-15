import { Component, OnInit } from '@angular/core';
import { Article } from "../../models/Article";
import { ArticleService } from "../../services/article/article.service";

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {
    articles: Article[];

    constructor(private articleService: ArticleService) {
    }

    ngOnInit() {
        this.articleService.getAllArticles().then(it => {
            this.articles = it
        })
    }

}
