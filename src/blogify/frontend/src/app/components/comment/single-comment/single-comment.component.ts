import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Comment } from '../../../models/Comment';
import { AuthService } from '../../../shared/auth/auth.service';
import { CommentsService, CommentTreeListing } from '../../../services/comments/comments.service';
import { ArticleService } from '../../../services/article/article.service';
import { faCommentAlt, faHeart, faTrashAlt } from '@fortawesome/free-regular-svg-icons';
import { faArrowCircleDown, faArrowDown, faHeart as faHeartFilled } from '@fortawesome/free-solid-svg-icons';
import { idOf } from '../../../models/Shadow';
import { ListingQuery } from '../../../models/ListingQuery';

@Component({
  selector: 'app-single-comment',
  templateUrl: './single-comment.component.html',
  styleUrls: ['./single-comment.component.scss']
})
export class SingleCommentComponent implements OnInit {

    @Input() parent: Comment;
    @Input() comment: Comment;

    @Output() deleted: EventEmitter<any> = new EventEmitter<any>();

    isReady = false;

    isLoggedInUsersComment = false;

    replyingEnabled = false;
    isDeleting = false;

    faCommentAlt = faCommentAlt;
    faHeartOutline = faHeart;
    faHeartFilled = faHeartFilled;

    faTrashAlt = faTrashAlt;

    faArrowDown = faArrowDown;

    constructor (
        private authService: AuthService,
        private commentsService: CommentsService,
        private articleService: ArticleService,
    ) {}

    /**
     * Stores the properties of {@link Article} that are needed for display in this component
     */
    private readonly REQUIRED_FIELDS: (keyof Comment)[] =
        ['uuid', 'commenter', 'article', 'content', 'likeCount', 'createdAt', 'parentComment'];

    /**
     * Use this listing for loading comments
     */
    listingQuery = { ...(new ListingQuery<Comment>(10, 0, this.REQUIRED_FIELDS)), depth: 9 };

    treeListing: CommentTreeListing;

    loggedInObs = this.authService.observeIsLoggedIn();

    async ngOnInit() {

        // Fetch full user instead of uuid only if it hasn't been fetched, or get it from parent if available

        if (this.parent && this.parent.commenter === this.comment.commenter) {
            this.comment.commenter = this.parent.commenter;
        } else if (typeof this.comment.commenter === 'string') {
            this.comment.commenter = await this.authService.fetchUser(this.comment.commenter);
        }

        // Article is always the same as parent

        if (this.parent) {
            this.comment.article = this.parent.article;
        } else if (typeof this.comment.article === 'string') {
            this.comment.article = await this.articleService.getArticleByUUID(this.comment.article);
        }

        // Make sure our children array is not undefined

        if (!this.comment.children)
            this.comment.children = { data: [], moreAvailable: false };

        this.isReady = true;

        // Handle new comments

        this.commentsService.newCommentsFromServer.subscribe(async payload => {
            if (payload && payload.parentComment) {
                if (payload.parentComment as unknown as string === this.comment.uuid)
                    this.comment.children.data.push(payload);
            }
        });

        // Update isLoggedInUsersComment

        this.authService.observeIsLoggedIn().subscribe(async state => {
            this.isLoggedInUsersComment = state && idOf(this.comment.commenter) === await this.authService.userUUID;
        });
    }

    loadPage() {
        this.listingQuery.page++;

        this.commentsService.commentTreeForComment(this.comment, this.listingQuery)
            .then(async comment =>
                this.comment.children = {
                    data: [...this.comment.children.data, ...comment.children.data],
                    moreAvailable: comment.children.moreAvailable
                }
            );
    }

    handleDeletion(comment: Comment) {
        this.comment.children.data = this.comment.children.data.filter(c => c.uuid !== comment.uuid);
    }

    toggleDeleting = () => this.isDeleting = !this.isDeleting;

    deleteSelf() {
        if (!this.isDeleting) return;

        this.commentsService
            .deleteComment(idOf(this.comment))
            .then(() => {
                this.deleted.emit();
            })
            .catch(() => {
                console.error(`[blogifyComments] Could not delete ${this.comment.uuid}`);
            });
    }

    toggleLike() {
        this.commentsService
            .toggleLike(this.comment)
            .then(_ => {
                this.comment.likedByUser = !this.comment.likedByUser;
                this.comment.likeCount += (this.comment.likedByUser ? 1 : -1);
            }).catch(() => {
                console.error(`[blogifyComments] Couldn't like ${this.comment.uuid}` );
            });
    }

}
