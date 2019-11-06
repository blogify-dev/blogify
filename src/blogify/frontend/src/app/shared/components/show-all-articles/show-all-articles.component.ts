import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../models/Article';
import { AuthService } from '../../auth/auth.service';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { StaticContentService } from '../../../services/static/static-content.service';
import { faArrowLeft, faPencilAlt, faSearch } from '@fortawesome/free-solid-svg-icons';
import { ArticleService } from '../../../services/article/article.service';

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
        private activatedRoute: ActivatedRoute,
        private router: Router
    ) {}

    ngOnInit() {
        this.activatedRoute.url.subscribe((it: UrlSegment[]) => {
            const isSearching = it[it.length - 1].parameters['search'] != undefined;
            if (isSearching) { // We are in a search page
                const query = it[it.length - 1].parameters['search'];
                const actualQuery = query.match(/"\w+"/) != null ? query.substring(1, query.length - 1): null;
                if (actualQuery != null) {
                    this.searchQuery = actualQuery;
                    this.startSearch();
                }
            } else { // We are in a regular listing
                this.stopSearch();
            }
        })
    }

    async navigateToSearch() {
        await this.router.navigate([{ search: `"${this.searchQuery}"` }], { relativeTo: this.activatedRoute })
    }

    private async startSearch() {
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
        if (!this.authService.isLoggedIn()) {
            const url = `/login?redirect=/article/new`;
            console.log(url);
            await this.router.navigateByUrl(url);
        } else {
            await this.router.navigateByUrl('/article/new');
        }
    }

}
