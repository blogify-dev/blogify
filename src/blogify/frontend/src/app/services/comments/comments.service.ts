import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Comment } from '../../models/Comment';
import { AuthService } from '../../shared/auth/auth.service';
import * as uuid from 'uuid/v4';
import { Article } from '../../models/Article';
import { BehaviorSubject } from 'rxjs';

const commentsEndpoint = '/api/articles/comments';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {
    private newRootComment = new BehaviorSubject<Comment>(undefined);

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    private async fetchUserObjects(comments: Comment[]): Promise<Comment[]> {
        const userUUIDs = new Set([...comments
            .filter (it => typeof it.commenter === 'string')
            .map    (it => it.commenter as string)]); // Converting to a Set makes sure a single UUID is not fetched more than once
        const userObjects = await Promise.all (
            [...userUUIDs].map(it => this.authService.fetchUser(it))
        );
        return comments.map(a => {
            a.commenter = userObjects
                .find(u => u.uuid === a.commenter as string);
            return a;
        });
    }

    private async fetchLikeStatus(comments: Comment[], userToken: string): Promise<Comment[]> {
        return Promise.all(comments.map(async c => {

            const httpOptions = {
                headers: new HttpHeaders({
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${userToken}`
                }),
            };

            // @ts-ignore
            this.httpClient.get<boolean>(`/api/articles/comments/${c.uuid}/like`, httpOptions).toPromise()
                .then((res: boolean) => {
                    c.likedByUser = res;
                }).catch(_ => {
                c.likedByUser = null;
            });
            return c;
        }));
    }

    private async prepareCommentData(comments: Comment[]): Promise<Comment[]> {
        return this
            .fetchUserObjects(comments)
            .then(a => this.authService.userToken ? this.fetchLikeStatus(a, this.authService.userToken) : a);
    }

    async getCommentsForArticle(article: Article): Promise<Comment[]> {
        const comments = await this.httpClient.get<Comment[]>(`${commentsEndpoint}/article/${article.uuid}`).toPromise();
        return comments ? this.prepareCommentData(comments) : [];
    }

    // tslint:disable-next-line
    async getCommentByUUID(uuid: string): Promise<Comment> {
        const comment = await this.httpClient.get<Comment>(`${commentsEndpoint}/${uuid}`).toPromise();
        comment.commenter = await this.authService.fetchUser(comment.commenter.toString());
        return comment;
    }

    async deleteComment(commentUUID: string, userToken: string = this.authService.userToken): Promise<object> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        return this.httpClient.delete(`${commentsEndpoint}/${commentUUID}`, httpOptions);
    }

    async createComment (
        commentContent: string,
        articleUUID: string,
        userUUID: string,
        userToken: string = this.authService.userToken
    ): Promise<Comment> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        const comment = {
            uuid: uuid(),
            commenter: userUUID,
            article: articleUUID,
            content: commentContent
        };

        return await this.httpClient.post<Comment>(`${commentsEndpoint}`, comment, httpOptions).toPromise();
    }

    async replyToComment (
        commentContent: string,
        articleUUID: string,
        userUUID: string,
        parentCommentUUID: string,
        userToken: string = this.authService.userToken,
    ) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        const comment = {
            uuid: uuid(),
            commenter: userUUID,
            article: articleUUID,
            content: commentContent,
            parentComment: parentCommentUUID
        };

        const res = await this.httpClient.post(`${commentsEndpoint}`, comment, httpOptions).toPromise();
        if (res == null) {
            return comment;
        } else {
            return undefined;
        }
    }

    async likeComment(comment: Comment, userToken: string): Promise<HttpResponse<object>> {

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
        return this.httpClient.post<HttpResponse<object>>(`/api/articles/comments/${comment.uuid}/like`, null, httpOptions).toPromise();
    }

    async getChildrenOf(commentUUID: string, depth: number): Promise<Comment> {
        return  this.httpClient.get<Comment>(`/api/articles/comments/tree/${commentUUID}/?depth=${depth}`).toPromise();
    }

    get latestRootSubmittedComment() {
        return this.newRootComment.asObservable();
    }

}
