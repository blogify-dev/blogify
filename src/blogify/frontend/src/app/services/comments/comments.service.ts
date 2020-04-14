import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Comment } from '../../models/Comment';
import { AuthService } from '../../shared/auth/auth.service';
import * as uuid from 'uuid/v4';
import { Article } from '../../models/Article';
import { BehaviorSubject } from 'rxjs';
import { idOf } from '../../models/Shadow';

const commentsEndpoint = '/api/articles/comments';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {
    private newComment = new BehaviorSubject<Comment>(undefined);

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    private async fetchUserObjects(comments: Comment[]): Promise<Comment[]> {
        // noinspection DuplicatedCode
        const userUUIDs = new Set([...comments
            .filter(it => typeof it.commenter === 'string')
            // Converting to a Set makes sure a single UUID is not fetched more than once
            .map(it => it.commenter as string)]);
        const userObjects = await Promise.all(
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

            this.httpClient.get<boolean>(`/api/articles/comments/${c.uuid}/like`, httpOptions).toPromise()
                .then((res: boolean) => {
                    c.likedByUser = res;
                }).catch(() => {
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
        const comments = await this.httpClient.get<Comment[]>(`${commentsEndpoint}/article/${article.uuid}`)
            .toPromise();
        return this.prepareCommentData(comments);
    }

    // tslint:disable-next-line:no-shadowed-variable
    async getCommentByUUID(uuid: string): Promise<Comment> {
        const comment = await this.httpClient.get<Comment>(`${commentsEndpoint}/${uuid}`).toPromise();

        comment.commenter = await this.authService.fetchUser(comment.commenter.toString());
        if (!comment.children) comment.children = [];

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

    async createComment(
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

        // noinspection JSDeprecatedSymbols
        const comment = {
            uuid: uuid(),
            commenter: userUUID,
            article: articleUUID,
            content: commentContent
        };

        return await this.httpClient.post<Comment>(`${commentsEndpoint}`, comment, httpOptions).toPromise();
    }

    async replyToComment(newComment: Comment) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${this.authService.userToken}`
            })
        };

        const comment = {
            uuid: uuid(),
            commenter: idOf(newComment.commenter),
            article: idOf(newComment.article),
            content: newComment.content,
            parentComment: newComment.parentComment ? idOf(newComment.parentComment) : null
        };

        return await this.httpClient.post(commentsEndpoint, comment, httpOptions).toPromise()
            .then(res => comment, err => undefined);
    }

    async likeComment(comment: Comment, userToken: string): Promise<HttpResponse<object>> {

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            }),
            observe: 'response',
        };

        return this.httpClient.post<HttpResponse<object>>(`/api/articles/comments/${comment.uuid}/like`,
            // TypeScript bug with method overloads.
            // @ts-ignore
            null, httpOptions).toPromise();
    }

    async getChildrenOf(commentUUID: string, depth: number): Promise<Comment> {
        return this.httpClient.get<Comment>(`/api/articles/comments/tree/${commentUUID}/?depth=${depth}`).toPromise();
    }

    async registerSubmittedComment(newCommentWsData: CommentCreatePayload) {
        this.newComment.next(await this.getCommentByUUID(newCommentWsData.uuid));
    }

    get latestSubmittedComment() {
        return this.newComment.asObservable();
    }

}

export interface CommentReplyPayload {
    newComment: string;
    onArticle: string;
    onComment: string | null;
}

export interface ArticleCommentReplyPayload {
    newComment: string;
    onArticle: string;
}

export interface CommentCreatePayload {
    uuid: string;
    article: string;
    commenter: string;
}
