import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../models/Article';
import { AuthService } from '../../auth/auth.service';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { StaticContentService } from '../../../services/static/static-content.service';
import { faArrowLeft, faPencilAlt, faSearch, faTimes} from '@fortawesome/free-solid-svg-icons';
import { ArticleService } from '../../../services/article/article.service';
import { User } from '../../../models/User';

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {

    faSearch = faSearch;
    faPencil = faPencilAlt;
    faArrowLeft = faArrowLeft;

    faTimes = faTimes;

    @Input() title = 'Articles';
    @Input() articles: Article[];
    @Input() noContentMessage = 'Nothing to see here !';
    @Input() noResultsMessage = 'No search results :(';
    @Input() forUser: User | null;
    @Input() allowCreate = true;

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
        private router: Router
    ) {}

    ngOnInit() {
        this.activatedRoute.url.subscribe((it: UrlSegment[]) => {
            const isSearching = it[it.length - 1].parameters['search'] != undefined;
            if (isSearching) { // We are in a search page
                const query = it[it.length - 1].parameters['search'];
                const actualQuery = query.match(/^"[^"']+"$/) != null ? query.substring(1, query.length - 1): null;
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

    async navigateToNoSearch() {
        await this.router.navigateByUrl(this.router.url.replace(/search/, '')) // Hacky, but works !
    }

    private async startSearch() {
        this.articleService.search (
            this.searchQuery,
            ['title', 'summary', 'createdBy', 'categories', 'createdAt'],
            this.forUser
        ).then(it => {
            this.searchResults = it;
            this.showingSearchResults = true;
            this.forceNoAllowCreate = true;
        }).catch((err: Error) => {
            console.error(`[blogifySearch] Error during search: ${err.name}: ${err.message}`)
        });
    }

    async stopSearch() {
        this.showingSearchResults = false;
        this.forceNoAllowCreate = false;
        this.searchQuery = undefined;
        this.showingMobileSearchBar = false;
        this.navigateToNoSearch();
    }

    async navigateToNewArticle() {
        this.authService.observeIsLoggedIn().subscribe(it => {
            if (it) this.router.navigateByUrl('/article/new');
            else this.router.navigateByUrl('/login?redirect=/article/new')
        });
    }

    setShowSearchBar(val: boolean) {
        this.showingMobileSearchBar = val
    }

}
