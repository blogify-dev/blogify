import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Article } from '@blogify/models/Article';
import { CommentsService } from '@blogify/core/services/comments/comments.service';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { Comment } from '@blogify/models/Comment';
import { idOf } from '@blogify/models/Shadow';

@Component({
    selector: 'app-create-comment',
    templateUrl: './create-comment.component.html',
    styleUrls: ['./create-comment.component.scss']
})
export class CreateCommentComponent implements OnInit {

    @Input() article: Article;
    @Input() comment: Comment;

    @Output() replied = new EventEmitter<any>();

    loggedInObs = this.authService.observeIsLoggedIn();

    replyComment: Comment;
    replyError: string;

    constructor(private commentsService: CommentsService, private authService: AuthService) {}

    async ngOnInit() {
        this.authService.observeIsLoggedIn().subscribe(async loggedIn => {
            this.replyComment = {
                commenter: loggedIn ? this.authService.currentUser.uuid : '',
                article: this.comment === undefined ? this.article.uuid : idOf(this.comment.article),
                parentComment: this.comment ? this.comment.uuid : undefined,
                likeCount: 0,
                likedByUser: false,
                content: '',
                uuid: '',
                createdAt: Date.now(),
                __type: ''
            };
        });
    }

    async doReply() {
        await this.commentsService.createComment(this.replyComment);

        this.replied.emit();
    }

}
