import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { Article, Content } from "../../models/Article";
import { ArticleService } from "../../services/article/article.service";
import { AuthService } from "../../services/auth/auth.service";
import { User } from "../../models/User";

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
        content: new Content('', ''),
        createdBy: new User('', ''),
        createdAt: Date.now(),
    };


    constructor(private articleService: ArticleService) {
    }

    ngOnInit() {
    }
     createNewArticle() {
        console.log(this.article);
        const obs = this.articleService.createNewArticle(this.article);
        obs.then(it => console.log(it))
    }



}
