/* tslint:disable:no-string-literal */
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Article} from '../../../models/Article';
import {Comment} from '../../../models/Comment';
import {CommentsService} from '../../../services/comments/comments.service';
import {AuthService} from '../../../services/auth/auth.service';

@Component({
    selector: 'app-create-comment',
    templateUrl: './create-comment.component.html',
    styleUrls: ['./create-comment.component.scss']
})
export class CreateCommentComponent implements OnInit {
    commentContent = '';
    @Input() article: Article;
    @Output() comment = new EventEmitter<Comment>();

    constructor(
        private commentsService: CommentsService,
        public authService: AuthService
    ) { }

    ngOnInit() {
    }

    async createCommentOnArticle() {
        this.commentsService.createComment(
            this.commentContent,
            this.article.uuid,
            this.authService.userUUID
        ).then((comment) => {
            console.log("fhdjk");
            console.log(comment);
            if (comment) {
                const newComment: Comment = {
                    uuid: comment['uuid'],
                    content: comment['content'],
                    article: this.article,
                    commenter: this.authService.userProfile,
                };
                console.log(`COMMENT POSTED : ${comment}`);
                console.log(newComment);
                this.comment.emit(newComment);
            } else {
                alert('Error occurred');
            }
        });
    }

    isLoggedIn(): boolean {
        return this.authService.userToken !== '';
    }

}
