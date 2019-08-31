export class Article {
    constructor(
        public uuid: string,
        public title: string,
        public content: Content,
        public createdBy: string,
        public createdAt: number,
        public categories: string[],
    ) { }
}

export interface Content {
    text: string;
    summary: string;
}