import { User } from './User';
import { Article } from './Article';
import { Shadow } from './Shadow';

export class Comment {

    constructor (
        public uuid: string,
        public commenter: Shadow<User>,
        public article: Shadow<Article>,
        public content: string,
        public likedByUser: boolean | null = null,
        public likeCount: number,
        public parentComment?: Shadow<Comment>,
        public children?: Comment[]
    ) {}

}
