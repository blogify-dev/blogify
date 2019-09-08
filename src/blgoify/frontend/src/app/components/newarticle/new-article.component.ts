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
        uuid: '5fb72569-2086-46b8-b8a9-828fe5ce1bb6' /*'9c22b1ea-983c-48db-abd3-bd9c70a9816e'*/,
        title: '',
        username: 'un',
        categories: [], // TODO: Get these from UI
        content: new Content('', ''),
        createdBy: { username: 'un', uuid: '747e5b83-72e7-4842-b373-918710fb3d86' },
        createdAt: Date.now(),
    };


    constructor(private articleService: ArticleService, private authService: AuthService) {
    }

    createNewArticle() {
        // TODO: Get this token automatically

        const token = this.authService.userToken;
        console.log(token);
        return this.articleService.createNewArticle(this.article, token).subscribe((it: any) => { //cant pass in interface
            this.article = it;
        });

    }

    ngOnInit() {
        //console.log(await this.authService.currentUserToken)
    }

}
