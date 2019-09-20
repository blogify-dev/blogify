import { Component, OnInit, Input } from '@angular/core';
import { CommentsService } from "../../services/comments/comments.service";
import { Comment } from "../../models/Comment";

@Component({
    selector: 'app-comment',
    templateUrl: './comment.component.html',
    styleUrls: ['./comment.component.scss']
})
export class CommentComponent implements OnInit {

    @Input() articleUUID: string;
    comments: Comment[];
    constructor(private commentService: CommentsService) {
    }

    ngOnInit() {
        this.commentService.getCommentsForArticle(this.articleUUID).toPromise().then(it => {
            this.comments = it
        })
    }

}
