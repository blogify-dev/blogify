import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Article, Content } from '../../models/Article'
import { Observable } from 'rxjs';
import { AuthService } from "../auth/auth.service";
import * as uuid from "uuid/v4";

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    async getAllArticles(): Promise<Article[]> {
        const articlesObs = this.httpClient.get<Article[]>('/api/articles/');
        const articles = await articlesObs.toPromise();
        const out: Article[] = [];

        for (const it of articles) {
            const copy = it;
            const promises = await Promise.all([
                this.authService.fetchUser(`${it.createdBy}`),
                this.getArticleContent(copy.uuid).toPromise()
            ]);
            copy.createdBy = promises[0];
            copy.content = promises[1];
            out.push(copy);
        }

        console.log(out);

        return out
    }

    getArticleByUUID(uuid: string): Observable<Article> {
        return this.httpClient.get<Article>(`/api/articles/${uuid}`)
    }

    async createNewArticle(article: Article, userToken: string = this.authService.userToken): Promise<Object> {

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userToken}`
            })
        };

        const {content, title, categories} = article;

        const newArticle = {
            uuid: uuid(),
            content: content,
            title: title,
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

        return this.httpClient.patch<Article>(`/api/articles/${uuid}`, article)
    }

    // noinspection JSUnusedGlobalSymbols
    deleteArticle(uuid: string) {
        return this.httpClient.delete(`api/articles/${uuid}`)
    }

    // noinspection JSUnusedGlobalSymbols
    getArticleContent(uuid: string) {
        return this.httpClient.get<Content>(`/api/articles/content/${uuid}`)
    }

}
