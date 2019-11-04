import {Component, Input, OnInit} from '@angular/core';
import {Article} from "../../../../models/Article";
import { faCommentAlt } from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'app-single-article-box',
    templateUrl: './single-article-box.component.html',
    styleUrls: ['./single-article-box.component.scss']
})
export class SingleArticleBoxComponent implements OnInit {

    @Input() article: Article;
    faCommentAlt = faCommentAlt;


    constructor() { }

    ngOnInit() { }

}
