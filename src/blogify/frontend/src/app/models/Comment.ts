import { User } from '@blogify/models/User';
import { Article } from '@blogify/models/Article';
import { Shadow } from '@blogify/models/Shadow';
import { CommentTreeListing } from '@blogify/core/services/comments/comments.service';

export class Comment {

    constructor (
        public uuid: string,
        public commenter: Shadow<User>,
        public article: Shadow<Article>,
        public content: string,
        public likedByUser: boolean | null = null,
        public likeCount: number,
        public createdAt: number,
        public parentComment?: Shadow<Comment>,
        public children?: CommentTreeListing,
    ) {}

}
