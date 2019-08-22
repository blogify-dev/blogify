import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {
    private articlesCommentsEndpoint = '/api/articles';

    constructor(private httpClient: HttpClient) { }

    getCommentsForArticle(uuid: string) {
        return this.httpClient.get(`${this.articlesCommentsEndpoint}/${uuid}`);
    }

    addComment(comment: Comment) {
        this.httpClient.post(this.articlesCommentsEndpoint, comment);
    }

    deleteComment(uuid: String) {
        this.httpClient.delete(`${this.articlesCommentsEndpoint}/${uuid}`);
    }

    editComment(uuid: String, comment: Comment){
        this.httpClient.patch(`${this.articlesCommentsEndpoint}/${uuid}`, comment);
    }

    getCommentContent(uuid: string) {
      return this.httpClient.get(`${this.articlesCommentsEndpoint}/content/${uuid}`);
    }
}
