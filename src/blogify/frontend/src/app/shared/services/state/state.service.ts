import { Injectable } from '@angular/core';
import {Article} from "../../../models/Article";
import {User} from "../../../models/User";

@Injectable({
    providedIn: 'root'
})
export class StateService {
    // uuid: Article
    private articles: { [key: string]: Article } = {  }

    // uuid: User
    private users: { [key: string]: User } = {}

    constructor() {}

    getArticle(uuid: string): Article | null {
        const article = this.articles[uuid]
        return article !== undefined ? article : null
    }

    cacheArticle(article: Article) {
        this.articles[article.uuid] = article
    }

    getUser(uuid: string): User | null {
        const user = this.users[uuid]
        return user !== undefined ? user : null
    }

    cacheUser(user: User) {
        this.users[user.uuid] = user
    }
}
