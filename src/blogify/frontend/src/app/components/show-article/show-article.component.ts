import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { Subscription } from 'rxjs';
import { Comment } from '../../models/Comment';
import { CommentsService } from '../../services/comments/comments.service';
import { AuthService } from '../../services/auth/auth.service';

@Component({
    selector: 'app-show-article',
    templateUrl: './show-article.component.html',
    styleUrls: ['./show-article.component.scss']
})
export class ShowArticleComponent implements OnInit {

    routeMapSubscription: Subscription;
    article: Article;

    comment: Comment = {
       commenter: '',
       article: '',
       uuid: '',
       content: ''
    };

    constructor (
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
        private commentsService: CommentsService,
        public authService: AuthService
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

    isLoggedIn(): boolean {
        return this.authService.userToken !== '';
    }

    convertTimeStampToHumanDate(time: number): string {
        return new Date(time).toDateString();
    }

    deleteArticle() {
        return this.articleService.deleteArticle(this.article.uuid);
    }

    async createCommentOnArticle() {
        this.comment.article = this.article.uuid;
        this.comment.commenter = this.authService.userUUID;
        this.commentsService.createComment(this.comment).then((comment: Comment) => console.log('COMMENT POSTED : ' + comment.content));
    }

}
