import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Article, Content } from '../../models/Article'
import { Observable } from 'rxjs';
import {AuthService} from "../auth/auth.service";
import {User} from "../../models/User";

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    auth: AuthService;

    constructor(private httpClient: HttpClient) {
    }

    getAllArticles(): Observable<Article[]> {
        return this.httpClient.get<Article[]>('/api/articles/')
    }

    getArticleByUUID(uuid: string): Observable<Article> {
        return this.httpClient.get<Article>(`/api/articles/${uuid}`)
    }

    createNewArticle(article: Article, userToken: string) { //
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userToken}`
            })
        };
        const {uuid, createdAt, content, title, categories, username} = article;
        const newArticle = {
            uuid: uuid,
            createdAt: createdAt,
            content: content,
            title: title,
            categories: categories,
            createdBy: article.createdBy.uuid,
            username: username
        };
        return this.httpClient.post(`/api/articles/`, newArticle, httpOptions)
    }

    updateArticle(uuid: string, article: Article, userToken: string) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userToken}`
            })
        };
        return this.httpClient.patch<Article>(`/api/articles/${uuid}`, article)
    }

    deleteArticle(uuid: string, userToken: string) {
        return this.httpClient.delete(`api/articles/${uuid}`)
    }

    getArticleContent(uuid: string) {
        return this.httpClient.get<Content>(`/api/articles/content/${uuid}`)
    }

}
