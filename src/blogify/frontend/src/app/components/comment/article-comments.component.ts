import { Component, OnInit, Input } from '@angular/core';
import { CommentsService } from '../../services/comments/comments.service';
import { Comment } from '../../models/Comment';
import { Article } from '../../models/Article';

@Component({
    selector: 'app-article-comments',
    templateUrl: './article-comments.component.html',
    styleUrls: ['./article-comments.component.scss']
})
export class ArticleCommentsComponent implements OnInit {

    @Input() private article: Article;

    comments: Comment[];

    constructor(private commentService: CommentsService) {}

    ngOnInit() {
        this.commentService.getCommentsForArticle(this.article).then(it => {
            this.comments = it;
        });
    }

    getNewComment(comment: Comment) {
        this.comments.push(comment);
    }
}
