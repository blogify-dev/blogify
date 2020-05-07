import { Injectable } from '@angular/core';
import { Article } from '../../../models/Article';
import { User } from '../../../models/User';

@Injectable({
    providedIn: 'root',
})
export class StateService {

    // uuid: Article
    private articles: { [key: string]: Article } = {}

    // uuid: User
    private users: { [key: string]: User } = {}

    constructor() {}

    getArticle(uuid: string): Article | null {
        return this.articles[uuid] ?? null;
    }

    cacheArticle(article: Article) {
        this.articles[article.uuid] = article;
    }

    clearArticles() {
        this.articles = {};
    }

    getUser(uuid: string): User | null {
        return this.users[uuid] ?? null;
    }

    cacheUser(user: User) {
        this.users[user.uuid] = user;
    }

    clearUsers() {
        this.users = {};
    }

}
