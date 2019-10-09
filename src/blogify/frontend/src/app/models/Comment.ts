import {User} from './User';
import {Article} from './Article';

export class Comment {

    constructor (
        public commenter: User | string,
        public article: Article,
        public content: string,
        public uuid: string,
        public parentComment?: Comment,
        public children?: Comment[]
    ) {}

}
