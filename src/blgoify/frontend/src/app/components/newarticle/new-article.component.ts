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

    user: User;


    article: Article = {
        uuid:'5fb72569-2086-46b8-b8a9-828fe5ce1bb6' /*'9c22b1ea-983c-48db-abd3-bd9c70a9816e'*/,
        title: '',
        username: 'un',
        categories: [], // TODO: Get these from UI
        content: new Content('', ''),
        createdBy: {username: 'lucy', uuid: '5fb72569-2086-46b8-b8a9-828fe5ce1bb6'},
        createdAt: Date.now(),
    };



    constructor(private articleService: ArticleService, private authService: AuthService) {
    }

    createNewArticle() {
        // TODO: Get this token automatically
        const token = 'n5r_kM33TIlGuvlsB-8GLMmmmDqYbRuvng7ZMze7dktUJsUmQMHP0MH7vSEChPoRhGzo6_rle4sr5Jf_Vrf8nw';
        return this.articleService.createNewArticle(this.article, token).subscribe((it: any)=>{
            this.article = it;
        });
    }

    async ngOnInit() {
        //console.log(await this.authService.currentUserToken)
    }

}
