import { Component, Input, OnInit } from '@angular/core';
import { Article } from "../../../../models/Article";
import { faHeart, faCommentAlt, faCopy } from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from "ngx-clipboard";

@Component({
    selector: 'app-single-article-box',
    templateUrl: './single-article-box.component.html',
    styleUrls: ['./single-article-box.component.scss']
})
export class SingleArticleBoxComponent implements OnInit {

    @Input() article: Article;
    faHeart = faHeart;
    faCommentAlt = faCommentAlt;
    faCopy = faCopy;

    constructor(private clipboardService: ClipboardService) {}

    ngOnInit() {}

    copyLinkToClipboard(article: Article) {
        this.clipboardService.copyFromContent(window.location.href)
    }
}
