import { Component, Input, OnInit } from '@angular/core';
import { Article } from '@blogify/models/Article';
import { faHeart, faCommentAlt, faClipboard } from '@fortawesome/free-regular-svg-icons';
import { faHeart as faHeartFilled, faThumbtack } from '@fortawesome/free-solid-svg-icons';
import { ClipboardService } from 'ngx-clipboard';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { ArticleService } from '@blogify/core/services/article/article.service';
import { EntityRenderComponent } from '@blogify/models/entities/EntityRenderComponent';

@Component({
    selector: 'app-single-article-box',
    templateUrl: './single-article-box.component.html',
    styleUrls: ['./single-article-box.component.scss']
})
export class SingleArticleBoxComponent extends EntityRenderComponent<Article> implements OnInit {

    faThumbtack = faThumbtack;

    faHeartOutline = faHeart;
    faHeartFilled = faHeartFilled;

    faCommentAlt = faCommentAlt;
    faClipboard = faClipboard;

    constructor (
        private authService: AuthService,
        private articleService: ArticleService,
        private clipboardService: ClipboardService
    ) { super(); }

    loggedInObs = this.authService.observeIsLoggedIn();

    ngOnInit() {}

    toggleLike() {
        this.articleService
            .likeArticle(this.entity)
            .then(() => {
                this.entity.likedByUser = !this.entity.likedByUser;
                this.entity.likeCount += (this.entity.likedByUser ? 1 : -1);
            }).catch(() => {
                console.error(`[blogifyArticles] Couldn't like ${this.entity.uuid}` );
            });
    }

    copyLinkToClipboard() {
        this.clipboardService.copyFromContent(window.location.href);
    }

}
