import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../models/Article';
import { CommentsService } from '../../../services/comments/comments.service';
import { AuthService } from '../../../shared/auth/auth.service';
import { Comment } from '../../../models/Comment';
import { idOf } from '../../../models/Shadow';

@Component({
    selector: 'app-create-comment',
    templateUrl: './create-comment.component.html',
    styleUrls: ['./create-comment.component.scss']
})
export class CreateCommentComponent implements OnInit {

    @Input() article: Article;
    @Input() comment: Comment;
    @Input() replying = false;

    loggedInObs = this.authService.observeIsLoggedIn();

    replyComment: Comment;
    replyError: string;

    constructor(private commentsService: CommentsService, private authService: AuthService) {}

    async ngOnInit() {
        this.authService.observeIsLoggedIn().subscribe(async value => {
            this.replyComment = {
                commenter: value ? (await this.authService.userProfile).uuid : '',
                article: this.comment === undefined ? this.article.uuid : idOf(this.comment.article),
                parentComment: this.comment ? this.comment.uuid : undefined,
                likeCount: 0,
                likedByUser: false,
                content: '',
                uuid: '',
                createdAt: Date.now(),
            };
        });
    }

    async doReply() {
        if (this.comment === undefined) { // Reply to article
            await this.commentsService.createComment(this.replyComment);

            this.replyComment.content = '';
        } else { // Reply to comment
            await this.commentsService.replyToComment(this.replyComment);

            this.replying = false;
        }
    }

}
