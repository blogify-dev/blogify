import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { Article, Content } from "../../models/Article";
import { ArticleService } from "../../services/article/article.service";
import { AuthService } from "../../services/auth/auth.service";
import { User } from "../../models/User";

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {


    routeMapSubscription: Subscription;
    user: User;

    article: Article = {
        uuid: this.user.uuid,
        title: '',
        categories: [], // TODO: Get these from UI
        content: new Content('', ''),
        createdBy: { username: this.user.username, uuid: this.user.uuid },
        createdAt: Date.now(),
    };


    constructor(private articleService: ArticleService, private authService: AuthService, private activatedRoute: ActivatedRoute) {
    }

    async ngOnInit() {
        console.log(this.authService.userToken);
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const userUUID = map.get('uuid');
            this.user = await this.authService.getUser(userUUID);
            console.log(userUUID);
            console.log(this.user)

        })
    }
     createNewArticle() {
        const token = this.authService.userToken;
        console.log(token);
        console.log(this.article);
        const obs = this.articleService.createNewArticle(this.article, token);
        obs.then(it => console.log(it))
    }



}
