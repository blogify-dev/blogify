import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Article } from '../../models/Article';
import { ListingQuery } from '../../models/ListingQuery';
import { AuthService } from '../../shared/auth/auth.service';
import * as uuid from 'uuid/v4';
import { User } from '../../models/User';
import { SearchView } from '../../models/SearchView';

interface ListingResult { data: Article[]; moreAvailable: boolean; }

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    private async fetchUserObjects(articles: Article[]): Promise<Article[]> {
        const userUUIDs = new Set([...articles
            .filter (it => typeof it.createdBy === 'string')
            .map    (it => it.createdBy as string)]); // Converting to a Set makes sure a single UUID is not fetched more than once
        const userObjects = await Promise.all (
            [...userUUIDs].map(it => this.authService.fetchUser(it))
        );
        return articles.map(a => {
            a.createdBy = userObjects
                .find(u => u.uuid === a.createdBy as string);
            return a;
        });
    }

    private async fetchLikeStatus(articles: Article[], userToken: string): Promise<Article[]> {
        return Promise.all(articles.map(async a => {

            const httpOptions = {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${userToken}`
                }),
            };

            // @ts-ignore
            this.httpClient.get<boolean>(`/api/articles/${a.uuid}/like`, httpOptions).toPromise()
            .then((res: boolean) => {
                a.likedByUser = res;
            }).catch(_ => {
                a.likedByUser = null;
            });
            return a;
        }));
    }

    private async prepareArticleData(articles: Article[]): Promise<Article[]> {
        return this
            .fetchUserObjects(articles)
            .then(a => this.authService.userToken ? this.fetchLikeStatus(a, this.authService.userToken) : a);
    }

    async getAllArticles(fields: string[] = [], amount: number = 25): Promise<Article[]> {
        const articlesObs = this.httpClient.get<Article[]>(`/api/articles/?fields=${fields.join(',')}&amount=${amount}`);
        const articles = await articlesObs.toPromise();

        return this.prepareArticleData(articles);
    }

    async getArticlesByListing(fields: (keyof Article)[] = [], listing: ListingQuery<Article>): Promise<ListingResult> {
        const listingObservable = this.httpClient.get<ListingResult>(`/api/articles/?quantity=${listing.quantity}&page=${listing.page}`);
        const result = await listingObservable.toPromise();

        return { data: await this.prepareArticleData(result.data), moreAvailable: result.moreAvailable };
    }

    async getArticleByUUID(articleUuid: string, fields: (keyof Article)[] = []): Promise<Article> {

        const actualFieldsString: string = fields.length === 0 ? '' : `?fields=${fields.join(',')}`;

        const article =  await this.httpClient.get<Article>(`/api/articles/${articleUuid}${actualFieldsString}`).toPromise();
        article.createdBy = await this.authService.fetchUser(article.createdBy.toString());
        return article;
    }

    async getArticleByForUser(username: string, fields: (keyof Article)[] = []): Promise<Article[]> {
        const articles = await this.httpClient.get<Article[]>(`/api/articles/forUser/${username}?fields=${fields.join(',')}`).toPromise();
        return this.prepareArticleData(articles);
    }

    async createNewArticle(article: Article, userToken: string = this.authService.userToken): Promise<any> {

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

        return this.httpClient.post<any>(`/api/articles/`, newArticle, httpOptions).toPromise();
    }

    async likeArticle(article: Article, userToken: string): Promise<HttpResponse<object>> {

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            }),
            observe: 'response',
        };

        // TypeScript bug with method overloads.
        // @ts-ignore
        // noinspection TypeScriptValidateTypes
        return this.httpClient.post<HttpResponse<object>>(`/api/articles/${article.uuid}/like`, null, httpOptions).toPromise();
    }

    updateArticle(article: Article, userToken: string = this.authService.userToken) {
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

        return this.httpClient.patch<Article>(`/api/articles/${article.uuid}`, newArticle, httpOptions).toPromise();
    }

    deleteArticle(articleUuid: string, userToken: string = this.authService.userToken) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };
        return this.httpClient.delete(`/api/articles/${articleUuid}`, httpOptions).toPromise();
    }

    search(query: string, fields: (keyof Article)[], byUser: User | null = null) {
        const byUserString = (byUser ? `&byUser=${byUser.uuid}` : '');
        const url = `/api/articles/search/?q=${query}&fields=${fields.join(',')}${byUserString}`;
        return this.httpClient.get<SearchView<Article>>(url)
            .toPromise()
            .then((hits) => {
                if (hits != null) {
                    return this.prepareArticleData(hits.hits.map(hit => hit.document));
                } else {
                    return Promise.all([]);
                }
            }); // Make sure user data is present
    }

    pinArticle(articleUuid: string, userToken: string = this.authService.userToken) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };
        return this.httpClient.post(`/api/articles/${articleUuid}/pin`, null, httpOptions).toPromise();
    }
}
