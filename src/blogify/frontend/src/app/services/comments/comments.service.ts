import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Comment} from '../../models/Comment';
import {AuthService} from '../auth/auth.service';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {
    private commentsEndpoint = '/api/articles/comments';

    constructor(private httpClient: HttpClient, private authService: AuthService) {
    }

    getCommentsForArticle(articleUUID: string) {
        return this.httpClient.get<Comment[]>(`${this.commentsEndpoint}/${articleUUID}`);
    }

    deleteComment(commentUUID: string, userToken: string = this.authService.userToken) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        return this.httpClient.delete(`${this.commentsEndpoint}/${commentUUID}`, httpOptions);
    }

    createComment(comment: Comment, userToken: string = this.authService.userToken) {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };
        return this.httpClient.post(`${this.commentsEndpoint}`, comment, httpOptions);
    }
}
