import { Component, OnInit, Input } from '@angular/core';
import { CommentsService } from '../../services/comments/comments.service';
import { Comment } from '../../models/Comment';
import { Article } from '../../models/Article';
import { AuthService } from '../../shared/auth/auth.service';

@Component({
    selector: 'app-article-comments',
    templateUrl: './article-comments.component.html',
    styleUrls: ['./article-comments.component.scss']
})
export class ArticleCommentsComponent implements OnInit {

    @Input() article: Article;

    rootComments: Comment[];

    constructor(private commentService: CommentsService, public authService: AuthService) {}

    ngOnInit() {
        this.commentService.getCommentsForArticle(this.article).then(async it => {
            this.rootComments = it;

            // Get promises for children of root comments
            const childrenPromises: Promise<Comment>[] = [];
            this.rootComments.forEach(comment => {
                childrenPromises.push(this.commentService.getChildrenOf(comment.uuid, 5));
            });
            const children = await Promise.all(childrenPromises);

            // Apply children to every root comment
            const out = [];
            this.rootComments.forEach((comment, index) => {
                comment.children = children[index].children;
                out.push(comment);
            });
            this.rootComments = out;

            console.log(out);
        });

        this.commentService.latestRootSubmittedComment.subscribe(comment => {
            if (comment) {
                this.rootComments.push(comment);
                console.log('Comment sub');
                console.log(comment);
            }
        });
    }

    isLoggedIn(): boolean {
        return this.authService.userToken !== '';
    }
}
