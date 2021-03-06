import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ListingQuery } from '@blogify/models/ListingQuery';
import { idOf, Shadow } from '@blogify/models/Shadow';
import { Comment } from '@blogify/models/Comment';
import { Article } from '@blogify/models/Article';
import { BehaviorSubject } from 'rxjs';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { UserService } from '@blogify/shared/services/user-service/user.service';

@Injectable({
    providedIn: 'root'
})
export class CommentsService {

    private commentsFromServer = new BehaviorSubject<Comment>(undefined);

    constructor(private httpClient: HttpClient, private authService: AuthService,        private userService: UserService,
    ) {}

    private readonly ENDPOINT = '/api/articles/comments';

    private readonly AUTH_HTTP_OPTIONS = () => {
        return { headers: new HttpHeaders({
            'Content-Type': 'application/json',
            Authorization: `Bearer ${this.authService.currentUser.token}`
        }) };
    }

    async commentTreeForArticle(article: Shadow<Article>, query: ListingQuery<Comment> & { depth: number }): Promise<CommentTreeListing> {
        return this.httpClient.get<CommentTreeListing> (
            `/api/articles/comments/tree/article/${idOf(article)}` +
            `?quantity=${query.quantity}` +
            `&page=${query.page}` +
            `&depth=${query.depth}` +
            (query.fields ? `&fields=${query.fields.join(',')}` : '')
        ).toPromise();
    }

    async commentTreeForComment(comment: Shadow<Comment>, query: ListingQuery<Comment> & { depth: number }): Promise<Comment> {
        return this.httpClient.get<Comment> (
            `/api/articles/comments/tree/comment/${idOf(comment)}` +
            `?quantity=${query.quantity}` +
            `&page=${query.page}` +
            `&depth=${query.depth}` +
            (query.fields ? `&fields=${query.fields.join(',')}` : '')
        ).toPromise();
    }

    // tslint:disable-next-line:no-shadowed-variable
    async getCommentByUUID(uuid: string): Promise<Comment> {
        const comment = await this.httpClient.get<Comment>(`${this.ENDPOINT}/${uuid}`).toPromise();

        comment.commenter = await this.userService.getUser(comment.commenter.toString());
        if (!comment.children) comment.children = { data: [], moreAvailable: false };

        return comment;
    }

    async createComment(newComment: Comment): Promise<Comment | undefined> {
        const comment = {
            commenter: this.authService.currentUser.uuid,
            article: idOf(newComment.article),
            content: newComment.content,
            parentComment: idOf(newComment.parentComment)
        };

        return await this.httpClient.post<Comment>(`${this.ENDPOINT}`, comment, this.AUTH_HTTP_OPTIONS()).toPromise()
            .then(() => comment as Comment, () => undefined);
    }

    async deleteComment(commentUUID: string): Promise<object> {
        return this.httpClient.delete(`${this.ENDPOINT}/${commentUUID}`, this.AUTH_HTTP_OPTIONS()).toPromise();
    }

    async toggleLike(comment: Comment): Promise<boolean> {
        return this.httpClient.post<string>(`/api/articles/comments/${comment.uuid}/like`, null, this.AUTH_HTTP_OPTIONS()).toPromise()
            .then(_ => true);
    }

    async registerCommentFromServer(newCommentWsData: CommentCreatePayload) {
        this.commentsFromServer.next(await this.getCommentByUUID(newCommentWsData.uuid));
    }

    get newCommentsFromServer() {
        return this.commentsFromServer.asObservable();
    }

}

// /tree/article/<id> uses this one -v
export interface CommentTreeListing {
    data: Comment[];
    moreAvailable: boolean;
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
