import { User } from './User';
import { Article } from './Article';
import { Shadow } from './Shadow';
import { CommentTreeListing } from '../services/comments/comments.service';

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
