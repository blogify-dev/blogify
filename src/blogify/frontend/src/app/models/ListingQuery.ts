import { Article } from '@blogify/models/Article';
import { Comment } from '@blogify/models/Comment';
import { User } from '@blogify/models/User';

export class ListingQuery<T extends Article | Comment | User> {

    constructor (
        public quantity: number,
        public page: number,
        public fields?: (keyof T)[],
    ) {}

}
