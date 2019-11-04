import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../models/Article';
import { AuthService } from '../../auth/auth.service';
import { Router } from '@angular/router';
import { StaticContentService } from '../../../services/static/static-content.service';
import { faArrowLeft, faPencilAlt, faSearch } from '@fortawesome/free-solid-svg-icons';
import {ArticleService} from '../../../services/article/article.service';

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {

    faSearch = faSearch;
    faPencil = faPencilAlt;
    faArrowLeft = faArrowLeft;

    @Input() title = 'Articles';
    @Input() articles: Article[];
    @Input() allowCreate = true;

    showingSearchResults = false;
    searchQuery: string;
    searchResults: Article[];

    constructor (
        private authService: AuthService,
        private articleService: ArticleService,
        private staticContentService: StaticContentService,
        private router: Router
    ) {}

    ngOnInit() {}

    async startSearch() {
        this.articleService.search (
            this.searchQuery,
            ['title', 'summary', 'createdBy', 'categories', 'createdAt']
        ).then(it => {
            this.searchResults = it;
            this.showingSearchResults = true;
        })
    }

    async stopSearch() {
        this.showingSearchResults = false;
    }

    async navigateToNewArticle() {
        if (this.authService.userToken === '') {
            const url = `/login?redirect=/new-article`;
            console.log(url);
            await this.router.navigateByUrl(url);
        } else {
            await this.router.navigateByUrl('/new-article');
        }
    }

}
