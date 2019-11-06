import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders} from '@angular/common/http';
import { Article } from '../../models/Article';
import { AuthService } from '../../shared/auth/auth.service';
import * as uuid from 'uuid/v4';

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    constructor(private httpClient: HttpClient, private authService: AuthService) {
    }

    async getAllArticles(fields: string[] = [], amount: number = 25): Promise<Article[]> {
        const articlesObs = this.httpClient.get<Article[]>(`/api/articles/?fields=${fields.join(',')}&amount=${amount}`);
        const articles = await articlesObs.toPromise();
        return this.fetchUserObjects(articles)
    }

    private async fetchUserObjects(articles: Article[]) {
        const userUUIDs = articles
            .filter (it => typeof it.createdBy === 'string')
            .map    (it => <string> it.createdBy);
        const userObjects = await Promise.all (
            [...userUUIDs].map(it => this.authService.fetchUser(it))
        );
        return articles.map(a => {
            a.createdBy = userObjects
                .find(u => u.uuid === <string> a.createdBy);
            return a
        });
    }

    async getArticleByUUID(uuid: string, fields: string[] = []): Promise<Article> {

        const actualFieldsString: string = fields.length === 0 ? "" : `?fields=${fields.join(',')}`;

        const article =  await this.httpClient.get<Article>(`/api/articles/${uuid}${actualFieldsString}`).toPromise();
        article.createdBy = await this.authService.fetchUser(article.createdBy.toString());
        return article;
    }

    async getArticleByForUser(username: string, fields: string[] = []): Promise<Article[]> {
        const articles = await this.httpClient.get<Article[]>(`/api/articles/forUser/${username}?fields=${fields.join(',')}`).toPromise();
        return this.fetchUserObjects(articles);
    }

    async createNewArticle(article: Article, userToken: string = this.authService.userToken): Promise<object> {

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        const {content, title, summary, categories} = article;

        const newArticle = {
            uuid: uuid(),
            content,
            title,
            summary,
            categories,
            createdBy: await this.authService.userUUID,
        };

        return this.httpClient.post(`/api/articles/`, newArticle, httpOptions).toPromise();
    }

    updateArticle(article: Article, uuid: string = article.uuid, userToken: string = this.authService.userToken) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        const newArticle = {
            uuid: article.uuid,
            content: article.content,
            title: article.title,
            summary: article.summary,
            categories: article.categories,
            createdBy: (typeof article.createdBy === 'string') ? article.createdBy : article.createdBy.uuid,
        };

        return this.httpClient.patch<Article>(`/api/articles/${uuid}`, newArticle, httpOptions).toPromise();
    }

    deleteArticle(uuid: string, userToken: string = this.authService.userToken) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };
        console.log('delete2');
        return this.httpClient.delete(`/api/articles/${uuid}`, httpOptions).toPromise();
    }

    search(query: string, fields: string[]) {
        const url = `/api/articles/search/?q=${query}&fields=${fields.join(',')}`;
        return this.httpClient.get<Article[]>(url)
            .toPromise()
            .then(articles => this.fetchUserObjects(articles)); // Make sure user data is present
    }

}
