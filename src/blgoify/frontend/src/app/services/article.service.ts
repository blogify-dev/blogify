import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Article } from '../models/Article';

@Injectable({
  providedIn: 'root'
})
export class ArticleService {
  private articlesEndpoint = '/api/articles';

  constructor(private httpClient: HttpClient) { }

  getAllArticles() {
    return this.httpClient.get(this.articlesEndpoint);
  }

  addArticle(article: Article) {
    this.httpClient.post(this.articlesEndpoint, article);
  }

  deleteArticle(uuid: string) {
    this.httpClient.delete(`${this.articlesEndpoint}/${uuid}`);
  }

  updateArticle(uuid: string, article: Article) {
    this.httpClient.patch(`${this.articlesEndpoint}/${uuid}`, article);
  }

  getArticleContent(uuid: string) {
    return this.httpClient.get(`${this.articlesEndpoint}/content/${uuid}`);
  }
}
