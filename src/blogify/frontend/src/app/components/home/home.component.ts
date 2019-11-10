import { Component, OnInit } from '@angular/core';
import { ArticleService } from '../../services/article/article.service';
import { Article } from '../../models/Article';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
    title = 'blogify';

    articles: Article[];

    constructor(private articleService: ArticleService) {}

    ngOnInit() {
        this.articleService.getAllArticles (
            ['title', 'summary', 'createdBy', 'categories', 'createdAt', 'numberOfComments']
        ).then( articles => {
            this.articles = articles;
            console.log(articles);
        });
    }


}
