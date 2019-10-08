import { Component, OnInit, Input } from '@angular/core';
import { CommentsService } from '../../services/comments/comments.service';
import { Comment } from '../../models/Comment';
import { Article } from '../../models/Article';
import {AuthService} from '../../services/auth/auth.service';

@Component({
    selector: 'app-article-comments',
    templateUrl: './article-comments.component.html',
    styleUrls: ['./article-comments.component.scss']
})
export class ArticleCommentsComponent implements OnInit {

    @Input() article: Article;

    comments: Comment[];

    constructor(private commentService: CommentsService, public authService: AuthService) {}

    ngOnInit() {
        this.commentService.getCommentsForArticle(this.article).then(async it => {
            this.comments = it;
            const childrenPromises: Promise<Comment>[] = [];
            this.comments.forEach(comment => {
                childrenPromises.push(this.commentService.getChildrenOf(comment.uuid, 3));
            });
            const children = await Promise.all(childrenPromises);
            const out = [];

            this.comments.forEach((comment, index) => {

                comment.children = children[index].children;
                comment.children.forEach(async child => {
                    child.commenter = await this.authService.fetchUser(child.commenter.toString());
                });
                out.push(comment);
            });
            this.comments = out;
            console.log(out);
        });
    }

    getNewComment(comment: Comment) {
        this.comments.push(comment);
    }

    isLoggedIn(): boolean {
        return this.authService.userToken !== '';
    }
}
