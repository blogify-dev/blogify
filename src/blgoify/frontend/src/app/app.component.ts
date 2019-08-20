import { Component } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { ArticleService } from './services/articles/article.service';
import { UsersService } from './services/users/users.service';
import { CommentsService } from './services/comments/comments.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'blogify';

    private urlArticles = '/api/articles';

    private urlUsers = '/api/users';

    private urlComments = '/api/comments/';

    articles;
    users;
    comments;
    headers = new HttpHeaders()
        .append('Content-Type', 'application/json');

    constructor(
        private http: HttpClient,
        private articleService: ArticleService,
        private usersService: UsersService,
        private commnetsService: CommentsService) { }

    async getArticles() {
        const articlesob = this.articleService.getAllArticles();
        this.articles = await articlesob.toPromise();
        console.log(this.articles);
        return this.articles;
    }

    async getUsers() {
        this.users = await this.usersService.getAllUsers();
        console.log(this.users);
    }

    async getComments() {
        const uuid = '85a1841f-143e-44cd-ad55-347207a60726';
        this.comments = await this.commnetsService.getCommentsForArticle(uuid).toPromise();
        console.log(this.comments);
    }
}
