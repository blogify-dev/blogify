import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Article } from '../../models/Article';

@Injectable({
    providedIn: 'root'
})
export class ArticleService {
    private articlesEndpint = '/api/articles';

    constructor(private httpClient: HttpClient) { }

    getAllArticles() {
        return this.httpClient.get(this.articlesEndpint);
    }

    addArticle(article: Article) {
        this.httpClient.post(this.articlesEndpint, article);
    }

    deleteArticle(uuid: string) {
        this.httpClient.delete(`${this.articlesEndpint}/${uuid}`);
    }

    updateArticle(uuid: string, article: Article) {
        this.httpClient.patch(`${this.articlesEndpint}/${uuid}`, article);
    }

    getArticleContent(uuid: string) {
        return this.httpClient.get(`${this.articlesEndpint}/content/${uuid}`);
    }
}
