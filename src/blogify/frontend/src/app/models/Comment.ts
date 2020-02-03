import { User } from './User';
import { Article } from './Article';

export class Comment {

    constructor (
        public uuid: string,
        public commenter: User | string,
        public article: Article,
        public content: string,
        public likedByUser: boolean | null = null,
        public likedImmediate: boolean = false,
        public likesCount: number,
        public parentComment?: Comment,
        public children?: Comment[]
    ) {}

}
