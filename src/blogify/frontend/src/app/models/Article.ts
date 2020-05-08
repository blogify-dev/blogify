import { User } from '@blogify/models/User';

export class Article {

    constructor (
        public uuid: string,
        public title: string,
        public content: string,
        public summary: string,
        public createdBy: User | string,
        public createdAt: number,
        public isPinned: boolean,
        public categories: Category[],
        public likedByUser: boolean | null = null,
        public likeCount: number = 0,
        public commentCount: number = 0,
    ) {}

}

export interface Category {
    name: string;
}
