import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {
    private articlesCommentsEndpint = '/api/articles';


    constructor(private httpClient: HttpClient) { }

    getCommentsForArticle(uuid: string) {
        return this.httpClient.get(`${this.articlesCommentsEndpint}/${uuid}`);
    }

    addComment() {
        // TODO: Backend not implemented
    }
}
