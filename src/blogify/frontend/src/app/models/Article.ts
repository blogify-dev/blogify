import { User } from '@blogify/models/User';
import { Entity } from '@blogify/models/entities/Entity';
import { SingleArticleBoxComponent } from '@blogify/shared/components/content-feed/single-article-box/single-article-box.component';
import { Type } from '@angular/core';

type SAB = SingleArticleBoxComponent;

export class Article implements Entity {
    
    constructor (
        public uuid: string,
        public title: string,
        public content: string,
        public summary: string,
        public createdBy: User | string,
        public createdAt: number,
        public isDraft: boolean = false,
        public isPinned: boolean,
        public categories: Category[],
        public likedByUser: boolean | null = null,
        public likeCount: number = 0,
        public commentCount: number = 0,
        public __type: string
    ) {}

}

export interface Category {
    name: string;
}
