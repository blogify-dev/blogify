import { Article } from './Article';
import { Comment } from './Comment';
import { User } from './User';

export class ListingQuery<TResource extends Article | User | Comment> {

    constructor (
        public quantity: number,
        public page: number,
        public forUser?: string,
        public searchQuery?: string
    ) {}

}
