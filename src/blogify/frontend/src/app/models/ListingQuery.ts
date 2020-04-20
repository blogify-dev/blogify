import { Article } from './Article';
import { Comment } from './Comment';
import { User } from './User';

export class ListingQuery<T extends Article | Comment | User> {

    constructor (
        public quantity: number,
        public page: number,
        public fields?: (keyof T)[],
    ) {}

}
