import { Component, Input, OnInit } from '@angular/core';
import { Comment } from '../../../models/Comment';
import { AuthService } from '../../../shared/auth/auth.service';
import { CommentsService } from '../../../services/comments/comments.service';
import { User } from '../../../models/User';
import { ArticleService } from '../../../services/article/article.service';
import { faHeart } from '@fortawesome/free-regular-svg-icons';
import { faHeart as faHeartFilled } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.scss']
})
export class SingleCommentComponent implements OnInit {

    @Input() parent: Comment;
    @Input() comment: Comment;
    @Input() child: boolean;

    isReady = false;

    replyingEnabled = false;
    replyComment: Comment;
    replyError: string;

    faHeartOutline = faHeart;
    faHeartFilled = faHeartFilled;

    constructor (
        private authService: AuthService,
        private commentsService: CommentsService,
        private articleService: ArticleService,
    ) {}

    loggedInObs = this.authService.observeIsLoggedIn();

    async ngOnInit() {

        // Fetch full user instead of uuid only if it hasn't been fetched, or get it from parent if available

        if (this.parent !== undefined && this.parent.commenter === this.comment.commenter) {
            this.comment.commenter = this.parent.commenter;
        } else if (typeof this.comment.commenter === 'string') {
            this.comment.commenter = await this.authService.fetchUser(this.comment.commenter);
        }

        // Article is always the same as parent

        if (this.parent !== undefined) {
            this.comment.article = this.parent.article;
        } else if (typeof this.comment.article === 'string') {
            this.comment.article = await this.articleService.getArticleByUUID(this.comment.article);
        }

        // Make sure our children array is not undefined

        if (!this.comment.children) this.comment.children = [];

        this.isReady = true;

        // Handle new comments

        this.commentsService.latestSubmittedComment.subscribe(async payload => {
            if (payload && payload.parentComment) {
                if (payload.parentComment as unknown as string === this.comment.uuid)
                    this.comment.children.push(payload);
            }
        });
    }

    toggleLike() {
        this.commentsService
            .likeComment(this.comment, this.authService.userToken)
            .then(() => {
                this.comment.likedByUser = !this.comment.likedByUser;
                this.comment.likeCount += (this.comment.likedByUser ? 1 : -1);
            }).catch(() => {
            console.error(`[blogifyComments] Couldn't like ${this.comment.uuid}` );
        });
    }

}
