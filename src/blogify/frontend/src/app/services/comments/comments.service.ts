import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Comment } from '../../models/Comment';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {
    private commentsEndpoint = '/api/articles/comments';

    constructor(private httpClient: HttpClient) {
    }

    getCommentsForArticle(articleUUID: string) {
        return this.httpClient.get<Comment[]>(`${this.commentsEndpoint}/${articleUUID}`);
    }

    deleteComment(commentUUID: string) {
        return this.httpClient.delete(`${this.commentsEndpoint}/${commentUUID}`);
    }

    createComment(comment: Comment) {
        return this.httpClient.post(`${this.commentsEndpoint}`, comment);
    }
}
