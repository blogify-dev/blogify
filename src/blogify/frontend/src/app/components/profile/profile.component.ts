import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from 'src/app/services/auth/auth.service';
import { User } from 'src/app/models/User';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {

    routeMapSubscription: Subscription;
    user: User;
    articles: Article[];

    constructor (
        private activatedRoute: ActivatedRoute,
        private authService: AuthService,
        private articleService: ArticleService,
    ) {}

    async ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {

            const username = map.get('username');

            this.user = await this.authService.getByUsername(username);

            this.articleService.getArticleByForUser(username,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt']
            ).then(it => {
                this.articles = it;
            });
        });
    }

    ngOnDestroy() {
        this.routeMapSubscription.unsubscribe();
    }

}
