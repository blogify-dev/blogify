import {Component, OnInit} from '@angular/core';
import {Article} from "../../models/Article";
import {ArticleService} from "../../services/article/article.service";
import {AuthService} from "../../services/auth/auth.service";
import {Router} from "@angular/router";
import { User } from "../../models/User";

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {
    articles: Article[];
    article: Article;



    user: User;

    constructor(
        private articleService: ArticleService,
        private authService: AuthService,
        private router: Router
    ) {
    }

    ngOnInit() {
        this.articleService.getAllArticles().then(it => {
            this.articles = it
        })
    }

    async navigateToNewArticle() {
        if (this.authService.userToken == '') {
            await this.router.navigateByUrl('/login')
        } else {
            await this.router.navigateByUrl('/new-article')

        }
    }


    deleteArticle(uuid: string){
        this.articleService.deleteArticle(this.article.uuid);
    }

}
