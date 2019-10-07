import {User} from './User';

export class Comment {

    constructor (
        public commenter: string,
        public article: string,
        public content: string,
        public uuid: string,
    ) {}

}
