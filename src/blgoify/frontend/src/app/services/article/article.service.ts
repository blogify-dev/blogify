import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Article, Content } from '../../models/Article'
import { Observable } from 'rxjs';
import { AuthService } from "../auth/auth.service";

@Injectable({
    providedIn: 'root'
})
export class ArticleService {
    constructor(private httpClient: HttpClient, private authService: AuthService) {
    }

    async getAllArticles() {
        const articlesObs = this.httpClient.get<Article[]>('/api/articles/');
        const articles = await articlesObs.toPromise();
        const out: Article[] = [];
        for (const it of articles) {
            const a = it;
            const createdBy = `${it.createdBy}`;
            console.log(createdBy);
            a.createdBy = await this.authService.getUser(createdBy);
            out.push(a);
        }
        console.log(out);
        return out
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
        const {uuid, content, title, categories} = article;
        const newArticle = {
            uuid: uuid,
            content: content,
            title: title,
            categories: categories,
            createdBy: article.createdBy.uuid,
        };
        console.log("yes");
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
