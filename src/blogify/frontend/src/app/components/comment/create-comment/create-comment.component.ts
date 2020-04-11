import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../models/Article';
import { CommentsService } from '../../../services/comments/comments.service';
import { AuthService } from '../../../shared/auth/auth.service';
import { User } from '../../../models/User';
import { Comment } from '../../../models/Comment';

@Component({
    selector: 'app-create-comment',
    templateUrl: './create-comment.component.html',
    styleUrls: ['./create-comment.component.scss']
})
export class CreateCommentComponent implements OnInit {

    commentContent = '';
    @Input() article: Article;
    @Input() comment: Comment;
    @Input() replying = false;

    replyComment: Comment;
    replyError: string;

    constructor(private commentsService: CommentsService, private authService: AuthService) {}

    async ngOnInit() {
        this.authService.observeIsLoggedIn().subscribe(async value => {
            this.replyComment = {
                commenter: value ? await this.authService.userProfile : '',
                article: this.comment === undefined ? this.article : this.comment.article,
                likeCount: 0,
                likedByUser: false,
                content: '',
                uuid: ''
            };
        });
    }

    async doReply() {
        // Make sure the user is authenticated
        if (this.authService.observeIsLoggedIn() && this.replyComment.commenter instanceof User) {
            if (this.comment === undefined) { // Reply to article
                await this.commentsService.createComment (
                    this.replyComment.content,
                    this.article.uuid,
                    this.replyComment.commenter.uuid
                );
            } else { // Reply to comment
                await this.commentsService.replyToComment (
                    this.replyComment.content,
                    this.comment.article.uuid,
                    this.replyComment.commenter.uuid,
                    this.comment.uuid
                );
            }

            this.replying = false;

        } else {
            this.replyError = 'You must be logged in to comment.';
        }
    }

}
