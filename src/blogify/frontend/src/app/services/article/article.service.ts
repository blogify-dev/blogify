import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Article } from '@blogify/models/Article';
import { ListingQuery } from '@blogify/models/ListingQuery';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { User } from '@blogify/models/User';
import { SearchView } from '@blogify/models/SearchView';
import { idOf, Shadow } from '@blogify/models/Shadow';
import { UserService } from '@blogify/shared/services/user-service/user.service';
import { EntityMetadata } from '@blogify/models/metadata/EntityMetadata';
import { EntityService } from '@blogify/core/services/EntityService';

interface ListingResult { data: Article[]; moreAvailable: boolean; }

@Injectable({
    providedIn: 'root'
})
export class ArticleService implements EntityService {

    constructor (
        private httpClient: HttpClient,
        private authService: AuthService,
        private userService: UserService
    ) {}

    private get HTTP_OPTIONS(): HttpHeaders {
        return this.authService.currentUser?.token ? new HttpHeaders({
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.authService.currentUser.token}`
        }) : new HttpHeaders({
            'Content-Type': 'application/json',
        });
    }

    private async fetchUserObjects(articles: Article[]): Promise<Article[]> {
        return Promise.all(articles.map(async article => {
            return this.userService.getUser(article.createdBy as string)
                .then(user => ({ ...article, createdBy: user }));
        }));
    }

    private async fetchLikeStatus(articles: Article[]): Promise<Article[]> {
        if (this.authService.currentUser === null) {
            return articles.map(article => {
                if (article.isDraft) return article;

                article.likedByUser = false;
                return article;
            });
        }

        return Promise.all(articles.map(async a => {
            if (a.isDraft) return a;

            return this.httpClient.get<boolean>(`/api/articles/${a.uuid}/like`, { headers: this.HTTP_OPTIONS }).toPromise()
                .then(likeStatus => ({ ...a, likedByUser: likeStatus }));
        }));
    }

    private async prepareArticleData(articles: Article[]): Promise<Article[]> {
        return this.fetchUserObjects(articles)
            .then(articlesWithLikes => this.fetchLikeStatus(articlesWithLikes));
    }

    async queryArticleListing(listing: ListingQuery<Article>): Promise<ListingResult> {
        const listingResultPromise = this.httpClient.get<ListingResult> (
            `/api/articles/?quantity=${listing.quantity}&page=${listing.page}`, { headers: this.HTTP_OPTIONS }
        ).toPromise();

        return listingResultPromise.then(async it => {
            const listingResult: ListingResult = { data: await this.prepareArticleData(it.data), moreAvailable: it.moreAvailable };

            return listingResult;
        });
    }

    async queryArticleListingForUser(listing: ListingQuery<Article> & { byUser?: Shadow<User> }): Promise<ListingResult> {
        const listingObservable = this.httpClient.get<ListingResult>(`/api/articles/user/${idOf(listing.byUser)}/?quantity=${listing.quantity}&page=${listing.page}`);
        const result = await listingObservable.toPromise();

        return { data: await this.prepareArticleData(result.data), moreAvailable: result.moreAvailable };
    }

    async queryArticleDraftsForUser (
        listing: ListingQuery<Article> & { byUser: Shadow<User>, fields?: (keyof Article)[] }
    ): Promise<ListingResult> {
        if (!this.authService.currentUser) throw 'not logged in';

        const listingObservable = this.httpClient.get<ListingResult> (
            `/api/users/me/drafts/articles?quantity=${listing.quantity}&page=${listing.page}${listing.fields ? `&fields=${listing.fields.join(',')}` : ''}`,
            { headers: this.HTTP_OPTIONS }
        );
        const result = await listingObservable.toPromise();

        return { data: await this.prepareArticleData(result.data), moreAvailable: result.moreAvailable };
    }

    async getArticle(articleUuid: string): Promise<Article> {
        const fetched = await this.httpClient.get<Article>(`/api/articles/${articleUuid}`).toPromise();

        console.log(`[article ${articleUuid}]: fetched`);

        return this.prepareArticleData([fetched]).then(arr => arr[0]);
    }

    createNewArticle(article: Article): Promise<any> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.currentUser.token}`
            })
        };

        const newArticle: Article = { ...article, createdBy: this.authService.currentUser.uuid };

        return this.httpClient.post<any>('/api/articles/', newArticle, httpOptions).toPromise();
    }

    updateArticle(article: Article): Promise<Article> {
        const newArticle: Article = { ...article, createdBy: this.authService.currentUser.uuid };

        return this.httpClient.patch<any>(`/api/articles/${article.uuid}`, newArticle, { headers: this.HTTP_OPTIONS }).toPromise();
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
            .then(hits => {
                if (hits != null) {
                    return this.prepareArticleData(hits.hits.map(hit => hit.document));
                } else {
                    return Promise.all([]);
                }
            }); // Make sure user data is present
    }

    likeArticle(article: Article): Promise<HttpResponse<object>> {
        return this.httpClient.post<HttpResponse<object>> (
            `/api/articles/${article.uuid}/like`, null, { headers: this.HTTP_OPTIONS }
        ).toPromise();
    }

    pinArticle(articleUuid: string): Promise<any> {
        return this.httpClient.post (
            `/api/articles/${articleUuid}/pin`, null, { headers: this.HTTP_OPTIONS }
        ).toPromise();
    }

    getMetadata(): Promise<EntityMetadata> {
        return this.httpClient.get<EntityMetadata>('/api/articles/_metadata').toPromise();
    }
    
}
