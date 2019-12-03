import { Component, Input, OnInit } from '@angular/core';
import { Article } from "../../../../models/Article";
import { faHeart, faCommentAlt, faCopy } from '@fortawesome/free-regular-svg-icons';
import { faHeart as faHeartFilled } from '@fortawesome/free-solid-svg-icons';
import { ClipboardService } from "ngx-clipboard";
import { AuthService } from '../../../auth/auth.service';
import { ArticleService } from '../../../../services/article/article.service';

@Component({
    selector: 'app-single-article-box',
    templateUrl: './single-article-box.component.html',
    styleUrls: ['./single-article-box.component.scss']
})
export class SingleArticleBoxComponent implements OnInit {

    @Input() article: Article;

    liked = false;

    faHeartOutline = faHeart;
    faHeartFilled = faHeartFilled;

    faCommentAlt = faCommentAlt;
    faCopy = faCopy;

    constructor (
        private authService: AuthService,
        private articleService: ArticleService,
        private clipboardService: ClipboardService
    ) {}

    ngOnInit() {}

    likeArticle() {
        this.articleService
            .likeArticle(this.article, this.authService.userToken)
            .then(_ => {
                this.liked = !this.liked;
            }).catch(error => {
                console.error(`[blogifyArticles] Couldn't like ${this.article.uuid}` )
            })
    }

    copyLinkToClipboard() {
        this.clipboardService.copyFromContent(window.location.href)
    }

}
