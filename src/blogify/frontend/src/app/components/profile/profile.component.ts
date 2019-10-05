import { Component, OnInit, OnDestroy } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from 'src/app/services/auth/auth.service';
import { User } from 'src/app/models/User';
import {Article} from "../../models/Article";
import {ArticleService} from "../../services/article/article.service";

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {
    routeMapSubscription: Subscription;
    user: User;
    articles: Article[];

    constructor(
        private activatedRoute: ActivatedRoute,
        private authService: AuthService,
        private articleService: ArticleService,
    ) {}

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const userUUID = map.get('uuid');
            this.user = this.authService.userProfile;
            this.articleService.getArticleByForUser(userUUID).then(it => {
                this.articles = it
            });
            console.log(userUUID);
            console.log(this.user)
        })


    }

    ngOnDestroy() {
        this.routeMapSubscription.unsubscribe()
    }

}
