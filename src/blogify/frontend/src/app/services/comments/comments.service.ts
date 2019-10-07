import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Comment } from '../../models/Comment';
import { AuthService } from '../auth/auth.service';

const commentsEndpoint = '/api/articles/comments';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {

    constructor(private httpClient: HttpClient, private authService: AuthService) {}

    async getCommentsForArticle(articleUUID: string): Promise<Comment[]> {
        return this.httpClient.get<Comment[]>(`${commentsEndpoint}/${articleUUID}`).toPromise();
    }

    async deleteComment(commentUUID: string, userToken: string = this.authService.userToken): Promise<Object> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        return this.httpClient.delete(`${commentsEndpoint}/${commentUUID}`, httpOptions);
    }

    async createComment(comment: Comment, userToken: string = this.authService.userToken): Promise<Object> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        return this.httpClient.post(`${commentsEndpoint}`, comment, httpOptions);
    }
}
