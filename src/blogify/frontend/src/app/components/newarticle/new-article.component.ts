import { Component, OnInit } from '@angular/core';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { User } from '../../models/User';
import { StaticFile } from "../../models/Static";
import { AuthService } from '../../shared/auth/auth.service';

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {

    article: Article = {
        uuid: '',
        title: '',
        categories: [],
        content: '',
        summary: '',
        createdBy: new User('', '', '', '', new StaticFile('-1')),
        createdAt: Date.now(),
    };
    user: User;

    constructor(private articleService: ArticleService, private authService: AuthService) {}

    async ngOnInit() {
        this.user = await this.authService.userProfile;
    }

    createNewArticle() {
        this.articleService.createNewArticle(this.article).then(article =>
            console.log(article)
        );
    }

    addCategory() {
        this.article.categories.push({name: ''});
    }
}
