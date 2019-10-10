import { User } from "./User";

export class Article {
    constructor(
        public uuid: string,
        public title: string,
        public content: string,
        public summary: string,
        public createdBy: User,
        public createdAt: number,
        public categories: object[],
    ) {}
}
