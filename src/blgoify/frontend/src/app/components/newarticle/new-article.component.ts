import { Component, OnInit } from '@angular/core';
import { Article, Content } from "../../models/Article";
import { ArticleService } from "../../services/article/article.service";
import { AuthService } from "../../services/auth/auth.service";

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {
    content: Content = {
        text: 'text', summary: 'summ'
    };
    article: Article = {
        uuid: '0911b916-8cee-49bd-be13-9bf4c6694cc1',
        title: '',
        categories: [{name: "yo"}, {'name': "nice"}], // TODO: Get these from UI
        content: this.content,
        createdBy: {username: 'un', uuid: 'a4003e55-6a2a-45d0-949d-41f13112caa9'},
        createdAt: Date.now()
    };

    constructor(private articleService: ArticleService, private authService: AuthService) {
    }

    createNewArticle() {
        // TODO: Get this token automatically
        const token = 'bV_GnFUPpJ2Dm1Ttg2h6AL0eLnS40JcjwnjX869haaP0yrlZGITisOOkuAFWCOhIC8c5jMuwWWHjJCA9KBo7QQ';
        return this.articleService.createNewArticle(this.article, token).toPromise();
    }

    async ngOnInit() {
        console.log(await this.authService.currentUserToken)
    }

}
