import { Component, Input, OnInit } from '@angular/core';
import { Comment } from '../../../models/Comment';
import { AuthService } from '../../../shared/auth/auth.service';
import { CommentsService } from '../../../services/comments/comments.service';
import { User } from '../../../models/User';
import { ArticleService } from '../../../services/article/article.service';

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.scss']
})
export class SingleCommentComponent implements OnInit {

    @Input() parent: Comment;
    @Input() comment: Comment;
    @Input() child: boolean;

    isReady: boolean = false;

    replyingEnabled: boolean = false;
    replyComment: Comment;
    replyError: string;

    constructor (
        private authService: AuthService,
        private commentsService: CommentsService,
        private articleService: ArticleService,
    ) {}

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


        this.isReady = true;

        // We're ready, so we can populate the dummy reply comment

        this.replyComment = {
            commenter: await this.authService.observeIsLoggedIn() ? await this.authService.userProfile : '',
            article: this.comment.article,
            content: '',
            uuid: ''
        };

    }

    async replyToSelf() {
        // Make sure the user is authenticated
        if (this.authService.observeIsLoggedIn() && this.replyComment.commenter instanceof User) {
            await this.commentsService.replyToComment (
                this.replyComment.content,
                this.comment.article.uuid,
                this.replyComment.commenter.uuid,
                this.comment.uuid
            );
        } else {
            this.replyError = 'You must be logged in to comment.'
        }
    }

    /**
     * @returns either the username of the comment, or it's UUID if it hasn't been fetched yet
     */
    usernameText(): string {
        if (typeof this.comment.commenter === 'string') return this.comment.commenter;
        else return this.comment.commenter.username
    }

}
