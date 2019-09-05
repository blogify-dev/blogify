import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Article } from '../../models/Article'
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    constructor(private httpClient: HttpClient) {
    }

    getAllArticles(): Observable<Article[]> {
        return this.httpClient.get<Article[]>('/api/articles/')
    }

    getArticleByUUID(uuid: string): Observable<Article> {
        return this.httpClient.get<Article>(`/api/articles/${uuid}`)
    }

    createNewArticle(article: Article, userToken: string) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userToken}`
            })
        };
        const {uuid, createdAt, content, title, categories} = article;
        const newArticle = {
            uuid: uuid,
            createdAt: createdAt,
            content: content,
            title: title,
            categories: categories,
            createdBy: article.createdBy.uuid
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

}
