import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article } from '../../models/Article';
import { Comment } from '../../models/Comment'
import { ArticleService } from '../../services/article/article.service';
import { Subscription } from 'rxjs';
import { AuthService } from '../../services/auth/auth.service';
import { CommentsService } from "../../services/comments/comments.service";
import { User } from "../../models/User";

@Component({
    selector: 'app-show-article',
    templateUrl: './show-article.component.html',
    styleUrls: ['./show-article.component.scss']
})
export class ShowArticleComponent implements OnInit {

    routeMapSubscription: Subscription;
    article: Article;
    user: User;
    comment: Comment = {
        commenter: this.user,
        article: this.article,
        content: '',
        uuid: ''
    };

    replyingEnabled: boolean = false;

    constructor (
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
        public authService: AuthService,
        private commentsService: CommentsService
    ) {}

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');
            console.log(articleUUID);

            this.article = await this.articleService.getArticleByUUID (
                articleUUID,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt']
            );


            console.log(this.article);
        });
    }

    convertTimeStampToHumanDate(time: number): string {
        return new Date(time).toDateString();
    }

    deleteArticle() {
        this.articleService.deleteArticle(this.article.uuid).then(it => console.log(it));
    }

    newComment() {
        this.commentsService.createComment(this.comment.content, this.article.uuid, this.user.uuid).then(comment =>
            console.log(comment));
    }


}
