import { Component, OnInit } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { ArticleService } from './services/article.service';
import { UsersService } from './services/users.service';
import { CommentsService } from './services/comments.service';
import { ContentService } from "./services/content.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'blogify';

  private urlArticles = '/api/articles';

  private urlUsers = '/api/users';

  private urlComments = '/api/comments/';

  articles;
  users;
  comments;
  content;
  headers = new HttpHeaders()
    .append('Content-Type', 'application/json');

  constructor(
    private http: HttpClient,
    private articleService: ArticleService,
    private usersService: UsersService,
    private commentsService: CommentsService,
    private contentService: ContentService) { }

  ngOnInit(){
    this.getArticles().then(r => Promise);
    this.getComments().then(r => Promise);
    this.getUsers().then(r=> Promise);
    this.getContent().then(r=> Promise);
  }

  async getArticles() {
    const articlesob = this.articleService.getAllArticles();
    this.articles = await articlesob.toPromise();
    console.log(this.articles);
    return this.articles;
  }

  async getContent(){
    const uuid = '85a1841f-143e-44cd-ad55-347207a60726';
    this.content = await this.contentService.getContent(uuid).toPromise();
    console.log(this.content);
  }

  async getUsers() {
    this.users = await this.usersService.getAllUsers().toPromise();
    console.log(this.users);
  }

  async getComments() {
    const uuid = '85a1841f-143e-44cd-ad55-347207a60726';
    this.comments = await this.commentsService.getCommentsForArticle(uuid).toPromise();
    console.log(this.comments);
  }
}
