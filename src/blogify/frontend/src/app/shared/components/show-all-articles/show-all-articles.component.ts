import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../models/Article';
import { ListingQuery } from '../../../models/ListingQuery';
import { ArticleService } from '../../../services/article/article.service';
import { AuthService } from '../../services/auth/auth.service';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { StaticContentService } from '../../../services/static/static-content.service';
import { faArrowDown, faArrowLeft, faPencilAlt, faSearch, faTimes } from '@fortawesome/free-solid-svg-icons';
import { User } from '../../../models/User';
import { Shadow } from '../../../models/Shadow';

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {

    /**
     * Stores the properties of {@link Article} that are needed for display in this component
     */
    private readonly REQUIRED_FIELDS: (keyof Article)[] =
        ['title', 'summary', 'createdAt', 'createdBy', 'categories', 'likeCount', 'commentCount'];

    faSearch = faSearch;
    faPencil = faPencilAlt;
    faArrowLeft = faArrowLeft;
    faTimes = faTimes;
    faArrowDown = faArrowDown;

    @Input() title = 'Articles';
    @Input() listingQuery: ListingQuery<Article> & { byUser?: Shadow<User> } = new ListingQuery(10, 0, this.REQUIRED_FIELDS);
    @Input() noContentMessage = 'Nothing to see here !';
    @Input() noResultsMessage = 'No search results :(';
    @Input() allowCreate = true;

    articles: Article[];
    moreAvailable: boolean;

    forceNoAllowCreate = false;

    showingSearchResults = false;
    searchQuery: string;
    searchResults: Article[];
    showingMobileSearchBar: boolean;

    constructor (
        private authService: AuthService,
        private articleService: ArticleService,
        private staticContentService: StaticContentService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
    ) {}

    ngOnInit() {
        this.activatedRoute.url.subscribe((it: UrlSegment[]) => {
            const isSearching = it[it.length - 1].parameters.search !== undefined;
            if (isSearching) { // We are in a search page
                const query = it[it.length - 1].parameters.search;
                const actualQuery = query.match(/^"[^"']+"$/) != null ? query.substring(1, query.length - 1) : null;
                if (actualQuery != null) {
                    this.searchQuery = actualQuery;
                    // noinspection JSIgnoredPromiseFromCall
                    this.startSearch();
                }
            } else { // We are in a regular listingl
                // noinspection JSIgnoredPromiseFromCall
                this.stopSearch();

                if (this.listingQuery.byUser)
                    this.articleService.queryArticleListingForUser(this.listingQuery)
                        .then(result => ({ data: this.articles, moreAvailable: this.moreAvailable } = result));
                 else
                    this.articleService.queryArticleListing(this.listingQuery)
                        .then(result => ({ data: this.articles, moreAvailable: this.moreAvailable } = result));
            }
        });
    }

    loadPage() {
        this.listingQuery.page++;

        if (this.listingQuery.byUser)
            this.articleService.queryArticleListingForUser(this.listingQuery)
                .then(result => {
                    this.articles.push(...result.data);
                    this.moreAvailable = result.moreAvailable;
                });
        else
            this.articleService.queryArticleListing(this.listingQuery)
                .then(result => {
                    this.articles.push(...result.data);
                    this.moreAvailable = result.moreAvailable;
                });
    }

    async navigateToSearch() {
        await this.router.navigate([{ search: `"${this.searchQuery}"` }], { relativeTo: this.activatedRoute });
    }

    async navigateToNoSearch() {
        await this.router.navigateByUrl(this.router.url.replace(/search/, '')); // Hacky, but works !
    }

    private async startSearch() {
        // this.articleService.getArticlesByListing(this.REQUIRED_FIELDS, new ListingQuery<Article>(this.listing.quantity, this.listing.page, this.listing.forUser, 'oooooooooo'))
        //     .then(result => {
        //         this.articles = [];
        //         this.articles.push(...result.data);
        //         this.moreAvailable = result.moreAvailable;
        //     });
        this.articleService.search (
            this.searchQuery,
            ['title', 'summary', 'createdBy', 'categories', 'createdAt'],
        ).then((result: Article[]) => {
            this.searchResults = result;
            this.showingSearchResults = true;
            this.forceNoAllowCreate = true;

        }).catch((err: Error) => {
            console.error(`[blogifySearch] Error during search: ${err.name}: ${err.message}`);
        });
    }

    async stopSearch() {
        this.showingSearchResults = false;
        this.forceNoAllowCreate = false;
        this.searchQuery = undefined;
        this.showingMobileSearchBar = false;
        await this.navigateToNoSearch();
    }

    async navigateToNewArticle() {
        this.authService.observeIsLoggedIn().subscribe(it => {
            if (it) this.router.navigateByUrl('/article/new');
            else this.router.navigateByUrl('/login?redirect=/article/new');
        });
    }

    setShowSearchBar(val: boolean) {
        this.showingMobileSearchBar = val;
    }

}
