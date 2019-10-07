import { Component, OnInit, Input } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';
import { CommentsService } from "../../services/comments/comments.service";
import { Comment } from "../../models/Comment";

@Component({
    selector: 'app-article-comments',
    templateUrl: './article-comments.component.html',
    styleUrls: ['./article-comments.component.scss']
})
export class ArticleCommentsComponent implements OnInit {

    @Input() private articleUUID: string;

    private comments: Comment[];

    constructor(private commentService: CommentsService, private authService: AuthService) {}

    ngOnInit() {
        this.commentService.getCommentsForArticle(this.articleUUID).then(it => {
            this.comments = it
        })
    }

}
