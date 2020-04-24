import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../../models/Article';
import { faHeart, faCommentAlt, faClipboard } from '@fortawesome/free-regular-svg-icons';
import { faHeart as faHeartFilled, faThumbtack } from '@fortawesome/free-solid-svg-icons';
import { ClipboardService } from 'ngx-clipboard';
import { AuthService } from '../../../services/auth/auth.service';
import { ArticleService } from '../../../../services/article/article.service';

@Component({
    selector: 'app-single-article-box',
    templateUrl: './single-article-box.component.html',
    styleUrls: ['./single-article-box.component.scss']
})
export class SingleArticleBoxComponent implements OnInit {

    @Input() article: Article;

    faThumbtack = faThumbtack;

    faHeartOutline = faHeart;
    faHeartFilled = faHeartFilled;

    faCommentAlt = faCommentAlt;
    faClipboard = faClipboard;

    constructor (
        private authService: AuthService,
        private articleService: ArticleService,
        private clipboardService: ClipboardService
    ) {}

    loggedInObs = this.authService.observeIsLoggedIn();

    ngOnInit() {}

    toggleLike() {
        this.articleService
            .likeArticle(this.article)
            .then(() => {
                this.article.likedByUser = !this.article.likedByUser;
                this.article.likeCount += (this.article.likedByUser ? 1 : -1);
            }).catch(() => {
                console.error(`[blogifyArticles] Couldn't like ${this.article.uuid}` );
            });
    }

    copyLinkToClipboard() {
        this.clipboardService.copyFromContent(window.location.href);
    }

}
