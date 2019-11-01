import {Component, OnInit} from '@angular/core';
import {Article} from "../../../models/Article";
import {ArticleService} from "../../../services/article/article.service";

@Component({
    selector: 'app-search',
    templateUrl: './search.component.html',
    styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {
    query = '';
    articles: Article[] = [];

    constructor(private articleService: ArticleService) {
    }

    ngOnInit() {
        console.log("search component")
    }

    async search() {
        if ((Math.random() * 4) > 3) {
            this.articles = await this.articleService.search(
                this.query,
                ['title', 'summary']
            )
        }
    }

    forceSearch() {
        this.articleService.search(
            this.query,
            ['title', 'summary']
        ).then(it => {
            console.log(it)
            this.articles = it
        })
    }
}
