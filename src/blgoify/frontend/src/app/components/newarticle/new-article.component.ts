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
    article: Article = {
        uuid: '9c22b1ea-983c-48db-abd3-bd9c70a9816e',
        title: '',
        categories: [{name: "yo"}, {'name': "nice"}], // TODO: Get these from UI
        content: new Content('text', 'summm'),
        createdBy: {username: 'un', uuid: 'a4003e55-6a2a-45d0-949d-41f13112caa9'},
        createdAt: Date.now()
    };

    constructor(private articleService: ArticleService, private authService: AuthService) {
    }

    createNewArticle() {
        // TODO: Get this token automatically
        const token = 'n5r_kM33TIlGuvlsB-8GLMmmmDqYbRuvng7ZMze7dktUJsUmQMHP0MH7vSEChPoRhGzo6_rle4sr5Jf_Vrf8nw';
        return this.articleService.createNewArticle(this.article, token).toPromise();
    }

    async ngOnInit() {
        console.log(await this.authService.currentUserToken)
    }

}
