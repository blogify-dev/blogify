import { Component, OnInit, Input } from '@angular/core';
import { CommentsService } from '../../services/comments/comments.service';
import { Comment } from '../../models/Comment';
import { Article } from '../../models/Article';
import { AuthService } from '../../shared/auth/auth.service';
import { faArrowAltCircleDown } from '@fortawesome/free-regular-svg-icons';


@Component({
    selector: 'app-article-comments',
    templateUrl: './article-comments.component.html',
    styleUrls: ['./article-comments.component.scss']
})
export class ArticleCommentsComponent implements OnInit {

    @Input() article: Article;

    rootComments: Comment[] = [];
    moreAvailable: boolean;

    faArrowAltCircleDown = faArrowAltCircleDown;
    private listingPage = 0;

    constructor(private commentService: CommentsService, public authService: AuthService) {}

    ngOnInit() {
        this.showCommentsOfArticle();

        this.commentService.latestRootSubmittedComment.subscribe(comment => {
            if (comment) {
                this.rootComments.push(comment);
            }
        });
    }

    showCommentsOfArticle() {
        this.commentService.getCommentsForArticle(this.article, { quantity: 10, page: this.listingPage }).then(async it => {
            this.rootComments = [...this.rootComments, ...it.data];
            this.moreAvailable = it.moreAvailable;

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
        });
    }

    loadPage() {
        this.listingPage++;
        this.showCommentsOfArticle();
    }

    isLoggedIn(): boolean {
        return this.authService.userToken !== '';
    }
}
