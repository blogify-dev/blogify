import { Component, Input, OnInit } from '@angular/core';
import { CommentsService, CommentTreeListing } from '../../services/comments/comments.service';
import { Comment } from '../../models/Comment';
import { Article } from '../../models/Article';
import { ListingQuery } from '../../models/ListingQuery';
import { faArrowDown } from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'app-article-comments',
    templateUrl: './article-comments.component.html',
    styleUrls: ['./article-comments.component.scss']
})
export class ArticleCommentsComponent implements OnInit {

    constructor (private commentsService: CommentsService) {}

    faArrowDown = faArrowDown;

    @Input() article: Article;

    /**
     * Stores the properties of {@link Article} that are needed for display in this component
     */
    private readonly REQUIRED_FIELDS: (keyof Comment)[] =
        ['uuid', 'commenter', 'article', 'content', 'likeCount', 'createdAt', 'parentComment'];

    /**
     * Use this listing for loading comments
     */
    listingQuery = { ...(new ListingQuery<Comment>(10, -1, this.REQUIRED_FIELDS)), depth: 9 };

    treeListing: CommentTreeListing;

    ngOnInit() {
        this.loadPage();

        this.commentsService.newCommentsFromServer.subscribe(async payload => {
            if (payload && !payload.parentComment) {
                this.treeListing.data = [payload, ...this.treeListing.data];
            }
        });
    }

    loadPage() {
        this.listingQuery.page++;

        this.commentsService.commentTreeForArticle(this.article, this.listingQuery)
            .then(async listing => this.treeListing = { data: [...this.treeListing?.data ?? [], ...listing.data], moreAvailable: listing.moreAvailable });
    }

    handleDeletion(comment: Comment) {
        this.treeListing.data.splice(this.treeListing.data.indexOf(comment), 1);
    }

}
