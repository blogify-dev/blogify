import {Component, Input, OnInit} from '@angular/core';
import { faArrowLeft, faSearch, faTimes } from '@fortawesome/free-solid-svg-icons';
import { AuthService} from '../../auth/auth.service';
import { StaticContentService } from '../../../services/static/static-content.service';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { User } from '../../../models/User';

@Component({
    selector: 'app-show-all-users',
    templateUrl: './show-all-users.component.html',
    styleUrls: ['./show-all-users.component.scss']
})
export class ShowAllUsersComponent implements OnInit {

    faSearch = faSearch;
    faArrowLeft = faArrowLeft;

    faTimes = faTimes;

    @Input() title = 'Users';
    @Input() users: User[];
    @Input() noContentMessage = 'Nothing to see here !';
    @Input() noResultsMessage = 'No search results :(';

    forceNoAllowCreate = false;

    showingSearchResults = false;
    searchQuery: string;
    searchResults: User[];
    showingMobileSearchBar: boolean;

    constructor(
        private authService: AuthService,
        private staticContentService: StaticContentService,
        private activatedRoute: ActivatedRoute,
        private router: Router
    ) {
    }

    ngOnInit() {
        /*this.authService.getAllUsers().then(it => {
            this.users = it;
            console.log(it);
        });*/
        this.activatedRoute.url.subscribe((it: UrlSegment[]) => {
            const isSearching = it[it.length - 1].parameters.search != undefined;
            if (isSearching) { // We are in a search page
                const query = it[it.length - 1].parameters.search;
                const actualQuery = query.match(/^"[^"']+"$/) != null ? query.substring(1, query.length - 1) : null;
                if (actualQuery != null) {
                    this.searchQuery = actualQuery;
                    this.startSearch();
                }
            } else { // We are in a regular listing
                this.stopSearch();
            }
        });
    }

    async navigateToSearch() {
        await this.router.navigate([{search: `"${this.searchQuery}"`}], {relativeTo: this.activatedRoute});
    }

    async navigateToNoSearch() {
        await this.router.navigateByUrl(this.router.url.replace(/search/, '')); // Hacky, but works !
    }

    private async startSearch() {
        this.authService.search (
            this.searchQuery,
            ['name', 'username', 'profilePicture'],
        ).then(it => {
            this.searchResults = it.hits.map(user => user.document);
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
        this.navigateToNoSearch();
    }


    setShowSearchBar(val: boolean) {
        this.showingMobileSearchBar = val;
    }

}
