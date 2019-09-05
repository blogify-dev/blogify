import {User} from "./User";

export class Article {
    constructor(
        public uuid: string,
        public title: string,
        public content: Content,
        public createdBy: User,
        public createdAt: number,
        public categories: object[],
    ) { }
}

export interface Content {
    text: string;
    summary: string;
}
