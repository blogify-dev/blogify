import { Component, Input, OnInit } from '@angular/core';
import { Comment } from '../../../models/Comment';
import { AuthService } from '../../../services/auth/auth.service';
import { CommentsService } from '../../../services/comments/comments.service';
import { User } from '../../../models/User';
import { ArticleService } from '../../../services/article/article.service';

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.scss']
})
export class SingleCommentComponent implements OnInit {

    @Input() comment: Comment;
    @Input() child: boolean;

    isReady: boolean = false;

    replyingEnabled: boolean = false;
    replyComment: Comment;
    replyError: string;

    constructor (private authService: AuthService,
                 private commentsService: CommentsService,
                 private articleService: ArticleService) {}

    async ngOnInit() {

        // Fetch full user instead of uuid only if it hasn't been fetched

        if (typeof this.comment.commenter === 'string') {
            this.comment.commenter = await this.authService.fetchUser(this.comment.commenter);
        }

        // Fetch full article instead of uuid only if it hasn't been fetched

        if (typeof this.comment.article === 'string') {
            this.comment.article = await this.articleService.getArticleByUUID(this.comment.article);
        }

        this.isReady = true;

        // We're ready, so we can populate the dummy reply comment

        this.replyComment = {
            commenter: this.authService.isLoggedIn() ? this.authService.userProfile : '',
            article: this.comment.article,
            content: '',
            uuid: ''
        };

        // console.log(`type: ${this.replyComment.commenter instanceof User}, logged in: ${this.authService.isLoggedIn()}, istype: ${this.authService.userProfile instanceof User}`);
    }

    async replyToSelf() {

        console.log(this.replyComment.commenter instanceof User);

        // Make sure the user is authenticated
        if (this.authService.isLoggedIn() && this.replyComment.commenter instanceof User) {
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
