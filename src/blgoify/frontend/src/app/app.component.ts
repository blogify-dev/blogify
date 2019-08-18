
import { Component } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'blogify';

  private url = 'localhost:8080/api';

  private urlArticles = '/api/articles';

  private urlUsers = '/api/users';

  private urlComments = '/api/comments/';

  articles;
  users;
  comments;
  headers = new HttpHeaders()
  .append('Content-Type', 'application/json');

  constructor(private http: HttpClient) {}

  async getArticles() {
    const articlesob = this.http.get(this.urlArticles, {headers: this.headers});
    this.articles = await articlesob.toPromise();
    console.log(this.articles);
    return this.articles;
  }

  async getUsers() {
    this.users = await this.http.get(this.urlUsers, {headers: this.headers}).toPromise();
    console.log(this.users);
  }

  async getComments() {
    this.comments = await this.http.get(`${this.urlComments}85a1841f-143e-44cd-ad55-347207a60726`, {headers: this.headers}).toPromise();
    console.log(this.comments);
  }
}
