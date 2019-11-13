import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import { ArticleService } from '../../services/article/article.service';
import { Article } from '../../models/Article';
import { Subscription } from 'rxjs';
import {User} from "../../models/User";
import {AuthService} from "../../shared/auth/auth.service";

@Component({
    selector: 'app-update-article',
    templateUrl: './update-article.component.html',
    styleUrls: ['./update-article.component.scss']
})
export class UpdateArticleComponent implements OnInit {

    routeMapSubscription: Subscription;
    article: Article;
    user: User;

    constructor(
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
        private router: Router,
        private authService: AuthService,
    ) { }

    ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');
            console.log(articleUUID);

            this.article = await this.articleService.getArticleByUUID(
                articleUUID,
                ['title', 'createdBy', 'content', 'summary', 'uuid', 'categories', 'createdAt']
            );

            console.log(this.article);
        });
        this.authService.userProfile.then(it => { this.user = it })
    }

    async updateArticle() {
        console.log(this.article);
        await this.articleService.updateArticle(this.article);
        await this.router.navigateByUrl(`/article/${this.article.uuid}`);
    }

    addCategory() {
        this.article.categories.push({name: ''});
    }
}
