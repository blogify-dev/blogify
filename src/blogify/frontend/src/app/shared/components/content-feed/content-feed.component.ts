import { Component, ComponentFactoryResolver, Input, OnInit, Type, ViewChild } from '@angular/core';
import { Article } from '@blogify/models/Article';
import { ListingQuery } from '@blogify/models/ListingQuery';
import { ArticleService } from '@blogify/core/services/article/article.service';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { StaticContentService } from '@blogify/core/services/static/static-content.service';
import {
    faArrowDown,
    faArrowLeft, faChevronDown,
    faFilter,
    faPencilAlt,
    faSearch,
    faSortAmountUp,
    faTimes, faTrash
} from '@fortawesome/free-solid-svg-icons';
import { User } from '@blogify/models/User';
import { Shadow } from '@blogify/models/Shadow';
import { ContentHostDirective } from '@blogify/shared/directives/content-host/content-host.directive';
import { SingleArticleBoxComponent } from '@blogify/shared/components/content-feed/single-article-box/single-article-box.component';
import { Entity } from '@blogify/models/entities/Entity';
import { SingleCommentComponent } from '@blogify/core/components/comment/single-comment/single-comment.component';
import { SingleUserBoxComponent } from '@blogify/shared/components/show-all-users/single-user-box/single-user-box.component';
import { EntityRenderComponent } from '@blogify/models/entities/EntityRenderComponent';
import { EntityMetadata } from '@blogify/models/metadata/EntityMetadata';
import { Filter } from '@blogify/shared/components/content-feed/filtering/Filter';
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";

@Component({
    selector: 'b-content-feed',
    templateUrl: './content-feed.component.html',
    styleUrls: ['./content-feed.component.scss']
})
export class ContentFeedComponent implements OnInit {

    /**
     * Stores the properties of {@link Article} that are needed for display in this component
     */
    private readonly REQUIRED_FIELDS: (keyof Article)[] =
        ['title', 'summary', 'createdAt', 'createdBy', 'categories', 'likeCount', 'commentCount', '__type'];

    faSearch = faSearch;
    faFilter = faFilter;
    faSortAmountUp = faSortAmountUp;

    faPencil = faPencilAlt;
    faArrowLeft = faArrowLeft;
    faTimes = faTimes;
    faArrowDown = faArrowDown;

    faTrashAlt = faTrashAlt;

    @Input() title = 'Articles';
    @Input() listingQuery: ListingQuery<Article> & { byUser?: Shadow<User> } = new ListingQuery(10, 0, this.REQUIRED_FIELDS);
    @Input() noContentMessage = 'Nothing to see here !';
    @Input() noResultsMessage = 'No search results :(';
    @Input() allowCreate = true;

    articles: Article[];
    moreAvailable: boolean;

    forceNoAllowCreate = false;

    showingFilteringMenu = false;

    showingSearchResults = false;
    searchQuery: string;
    searchResults: Article[];
    showingMobileSearchBar: boolean;

    entityModel: EntityMetadata = {
        entity: {
            isSearchable: false
        },
        properties: {},
    };

    activeFilters: Filter[] = [];

    modelFilterableProps = () => Object.entries(this.entityModel.properties)
        .filter(prop => prop[1].filtering.isFilterable)
        .map(prop => ({ ...prop[1], name: prop[0] }))

    @ViewChild(ContentHostDirective, { static: false }) contentHost: ContentHostDirective;

    // This decides, based on the __type property of the entity, which component to use for rendering it
    private componentRenderers = {
        article: SingleArticleBoxComponent,
        comment: SingleCommentComponent,
        user: SingleUserBoxComponent
    }
    findRenderer = (a: Entity) => {
        if (!a['__type']) {
            console.error('[blogifyContentFeed] entity had no __type property');
            return undefined;
        }
        return this.componentFactoryResolver.resolveComponentFactory(this.componentRenderers[a['__type']]);
    };

    constructor (
        private authService: AuthService,
        private articleService: ArticleService,
        private staticContentService: StaticContentService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private componentFactoryResolver: ComponentFactoryResolver
    ) {}

    ngOnInit() {
        this.articleService.getMetadata().then(metadata => this.entityModel = metadata);

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
            } else { // We are in a regular listing
                // noinspection JSIgnoredPromiseFromCall
                this.stopSearch();

                if (this.listingQuery.byUser)
                    this.articleService.queryArticleListingForUser(this.listingQuery)
                        .then(result => {
                            this.displayContent(result.data);
                            this.moreAvailable = result.moreAvailable;
                        });
                else
                    this.articleService.queryArticleListing(this.listingQuery)
                        .then(result => {
                            this.displayContent(result.data);
                            this.moreAvailable = result.moreAvailable;
                        });
            }
        });
    }

    loadPage() {
        this.listingQuery.page++;

        if (this.listingQuery.byUser)
            this.articleService.queryArticleListingForUser(this.listingQuery)
                .then(result => {
                    this.displayContent(result.data);
                    this.moreAvailable = result.moreAvailable;
                });
        else
            this.articleService.queryArticleListing(this.listingQuery)
                .then(result => {
                    this.displayContent(result.data);
                    this.moreAvailable = result.moreAvailable;
                });
    }

    private displayContent<TEntity extends Entity>(content: TEntity[]) {
        content.forEach(entity => {
            const factory = this.findRenderer(entity);
            if (factory) {
                const entityComponentInstance = this.contentHost.viewContainerRef.createComponent(factory).instance as EntityRenderComponent<TEntity>;

                entityComponentInstance.entity = entity;
            }
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

    toggleShowingFilteringMenu = () => this.showingFilteringMenu = !this.showingFilteringMenu;

    setShowSearchBar(val: boolean) {
        this.showingMobileSearchBar = val;
    }

}
