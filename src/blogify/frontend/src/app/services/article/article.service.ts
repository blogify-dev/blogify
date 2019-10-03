import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Article } from '../../models/Article'
import { Observable } from 'rxjs';
import { AuthService } from "../auth/auth.service";
import * as uuid from "uuid/v4";

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    async getAllArticles(fields: string[] = [], amount: number = 25): Promise<Article[]> {
        const articlesObs = this.httpClient.get<Article[]>(`/api/articles/?fields=${fields.join(',')}&amount=${amount}`);
        return articlesObs.toPromise();
    }

    async getArticleByUUID(uuid: string, fields: string[] = []): Promise<Article> {
        return this.httpClient.get<Article>(`/api/articles/${uuid}?fields=${fields.join(',')}`).toPromise()
    }

    async getArticleByForUser(uuid: string): Promise<Article[]> {
        return this.httpClient.get<Article[]>(`/api/articles/forUser/${uuid}?fields=title,createdBy,content,summary,uuid,categories`).toPromise();
    }

    async createNewArticle(article: Article, userToken: string = this.authService.userToken): Promise<Object> {

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userToken}`
            })
        };

        const {content, title, summary, categories} = article;

        const newArticle = {
            uuid: uuid(),
            content: content,
            title: title,
            summary: summary,
            categories: categories,
            createdBy: this.authService.userUUID,
        };

        return this.httpClient.post(`/api/articles/`, newArticle, httpOptions).toPromise()
    }

    // noinspection JSUnusedGlobalSymbols
    updateArticle(uuid: string, article: Article, userToken: string) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userToken}`
            })
        };

        return this.httpClient.patch<Article>(`/api/articles/${uuid}`, article, httpOptions)
    }

    // noinspection JSUnusedGlobalSymbols
    deleteArticle(uuid: string) {
        return this.httpClient.delete(`api/articles/${uuid}`)
    }

}
