import { Component, OnInit } from '@angular/core';
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

    user: User = {
        username: 'lucy',
        uuid: '5fb72569-2086-46b8-b8a9-828fe5ce1bb6'
    };


    article: Article = {
        uuid: '62fef444-570f-46e0-96b4-31a41238049b' /*'9c22b1ea-983c-48db-abd3-bd9c70a9816e'*/,
        title: '',
        categories: [], // TODO: Get these from UI
        content: new Content('', ''),
        createdBy: { username: 'un', uuid: 'aa6e4b49-29c5-4028-a99e-96d5f93ef8ff' },
        createdAt: Date.now(),
    };


    constructor(private articleService: ArticleService, private authService: AuthService) {
    }

    createNewArticle() {
        const token = this.authService.userToken;
        console.log(token);
        console.log(this.article);
        const obs = this.articleService.createNewArticle(this.article, token);
        obs.subscribe(it => console.log(it))
    }

    ngOnInit() {
        //console.log(await this.authService.currentUserToken)
    }

}
