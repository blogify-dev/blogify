import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Article } from '../../models/Article'

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    constructor(private httpClient: HttpClient) { }

    getAllArticles() {
        return this.httpClient.get<Article>('/api/articles/')
    }

    getArticleByUUID(uuid: string) {
        return this.httpClient.get<Article>(`/api/articles/${uuid}`)
    }

    createNewArticle(article: Article, userToken: string) {
        const httpOptions = {
            headers: new HttpHeaders({
              'Content-Type':  'application/json',
              'Authorization': `Bearer ${userToken}`
            })
          };
        return this.httpClient.post<Article>(`/api/articles/`, article, httpOptions)
    }

    updateArticle(uuid: string, article: Article, userToken: string) {
        const httpOptions = {
            headers: new HttpHeaders({
              'Content-Type':  'application/json',
              'Authorization': `Bearer ${userToken}`
            })
          };
        return this.httpClient.patch<Article>(`/api/articles/${uuid}`, article)
    }

    deleteArticle(uuid: string, userToken: string) {
        return this.httpClient.delete(`api/articles/${uuid}`)
    }
 
}
