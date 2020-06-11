import { User } from '@blogify/models/User';
import { Article } from '@blogify/models/Article';
import { Shadow } from '@blogify/models/Shadow';
import { CommentTreeListing } from '@blogify/core/services/comments/comments.service';
import { Entity } from '@blogify/models/entities/Entity';

export class Comment implements Entity {
    
    constructor (
        public uuid: string,
        public commenter: Shadow<User>,
        public article: Shadow<Article>,
        public content: string,
        public likedByUser: boolean | null = null,
        public likeCount: number,
        public createdAt: number,
        public __type: string,
        public parentComment?: Shadow<Comment>,
        public children?: CommentTreeListing,
    ) {}

}
