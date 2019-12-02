import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Comment } from '../../models/Comment';
import { AuthService } from '../../shared/auth/auth.service';
import * as uuid from 'uuid/v4';
import { Article } from '../../models/Article';

const commentsEndpoint = '/api/articles/comments';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    async getCommentsForArticle(article: Article): Promise<Comment[]> {
        const comments = await this.httpClient.get<Comment[]>(`${commentsEndpoint}/article/${article.uuid}`).toPromise();
        const userUUIDs = new Set<string>();
        comments.forEach(it => {
            userUUIDs.add(it.commenter.toString());
        });
        const users = await Promise.all(([...userUUIDs]).map(it => this.authService.fetchUser(it.toString())));
        comments.map(it => {
            it.article = article;
            it.commenter = users.find((user) => user.uuid === it.commenter.toString());
        });
        return comments;
    }

    async getComment(uuid: string): Promise<Comment> {
        const comment = await this.httpClient.get<Comment>(`${commentsEndpoint}/${uuid}`).toPromise();
        comment.commenter = await this.authService.fetchUser(comment.commenter.toString())
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

        const comment = {
            uuid: uuid(),
            commenter: userUUID,
            article: articleUUID,
            content: commentContent
        };

        const res = await this.httpClient.post<Comment>(`${commentsEndpoint}`, comment, httpOptions).toPromise();
        console.log('---------Comment------');
        console.log(res);
        return res;
    }

    async replyToComment(
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

    async getChildrenOf(commentUUID: string, depth: number): Promise<Comment> {
        return  this.httpClient.get<Comment>(`/api/articles/comments/tree/${commentUUID}/?depth=${depth}`).toPromise();
    }
}
