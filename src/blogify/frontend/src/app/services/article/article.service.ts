import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Article } from '../../models/Article';
import { ListingQuery } from '../../models/ListingQuery';
import { AuthService } from '../../shared/auth/auth.service';
import { User } from '../../models/User';
import { SearchView } from '../../models/SearchView';
import { idOf, Shadow } from '../../models/Shadow';
import { StateService } from '../../shared/services/state/state.service';
import { UserService } from '../../shared/services/user-service/user.service';

interface ListingResult { data: Article[]; moreAvailable: boolean; }

@Injectable({
    providedIn: 'root'
})
export class ArticleService {

    constructor (
        private httpClient: HttpClient,
        private authService: AuthService,
        private state: StateService,
        private userService: UserService
    ) {}

    private async fetchUserObjects(articles: Article[]): Promise<Article[]> {
        return Promise.all(articles.map(async article => {
            return this.userService.getUser(article.createdBy as string)
                .then(user => ({ ...article, createdBy: user }));
        }));
    }

    private async fetchLikeStatus(articles: Article[]): Promise<Article[]> {
        if (this.authService.currentUser === null) {
            return articles.map(article => {
                article.likedByUser = false
                return article
            })
        }
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            }),
        };

        return Promise.all(articles.map(async a => {
            return this.httpClient.get<boolean>(`/api/articles/${a.uuid}/like`, httpOptions).toPromise()
                .then(likeStatus => ({ ...a, likedByUser: likeStatus }));
        }));
    }

    private async prepareArticleData(articles: Article[]): Promise<Article[]> {
        return this.fetchUserObjects(articles)
            .then(articlesWithLikes => this.fetchLikeStatus(articlesWithLikes));
    }

    async queryArticleListing(listing: ListingQuery<Article>): Promise<ListingResult> {
        const listingResultPromise = this.httpClient.get<ListingResult>(`/api/articles/?quantity=${listing.quantity}&page=${listing.page}`).toPromise();

        return listingResultPromise.then(async it => {
            const listingResult: ListingResult = { data: await this.prepareArticleData(it.data), moreAvailable: it.moreAvailable };

            listingResult.data.forEach(article => this.state.cacheArticle(article));

            return listingResult;
        });
    }

    async queryArticleListingForUser(listing: ListingQuery<Article> & { byUser?: Shadow<User> }): Promise<ListingResult> {
        const listingObservable = this.httpClient.get<ListingResult>(`/api/articles/user/${idOf(listing.byUser)}/?quantity=${listing.quantity}&page=${listing.page}`);
        const result = await listingObservable.toPromise();

        return { data: await this.prepareArticleData(result.data), moreAvailable: result.moreAvailable };
    }

    async getArticle(articleUuid: string): Promise<Article> {
        const cached = this.state.getArticle(articleUuid);

        if (cached) {
            console.log(`[article ${articleUuid}]: returning from cache`);

            return cached;
        } else {
            const fetched = await this.httpClient.get<Article>(`/api/articles/${articleUuid}`).toPromise();
            this.state.cacheArticle(fetched);

            console.log(`[article ${articleUuid}]: fetched`);

            return this.prepareArticleData([fetched]).then(arr => arr[0]);
        }
    }

    createNewArticle(article: Article): Promise<any> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            })
        };


        const newArticle: Article = { ...article, createdBy: this.authService.currentUser.uuid };

        return this.httpClient.post<any>(`/api/articles/`, newArticle, httpOptions).toPromise();
    }

    updateArticle(article: Article): Promise<Article> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            })
        };

        const newArticle: Article = { ...article, createdBy: this.authService.currentUser.uuid };

        return this.httpClient.patch<any>(`/api/articles/${article.uuid}`, newArticle, httpOptions).toPromise();
    }

    deleteArticle(articleUuid: string): Promise<any> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            })
        };
        return this.httpClient.delete<any>(`/api/articles/${articleUuid}`, httpOptions).toPromise();
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

    likeArticle(article: Article): Promise<HttpResponse<object>> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            }),
            observe: 'response',
        };

        // TypeScript bug with method overloads.
        // @ts-ignore
        // noinspection TypeScriptValidateTypes
        return this.httpClient.post<HttpResponse<object>>(`/api/articles/${article.uuid}/like`, null, httpOptions).toPromise();
    }

    pinArticle(articleUuid: string): Promise<any> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            })
        };

        return this.httpClient.post(`/api/articles/${articleUuid}/pin`, null, httpOptions).toPromise();
    }

}
