import { Component, OnInit } from '@angular/core';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { User } from '../../models/User';

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {

    article: Article = {
        uuid: '',
        title: '',
        categories: [], // TODO: Get these from UI
        content: 'new Content()',
        summary: '',
        createdBy: new User('', '', '', ''),
        createdAt: Date.now(),
    };


    constructor(private articleService: ArticleService) {
    }

    ngOnInit() {
    }

     createNewArticle() {
        console.log(this.article);
        this.articleService.createNewArticle(this.article).then( article =>
            console.log(article)
        );
    }

    addCategory() {
        this.article.categories.push({name: ''});
    }
}
