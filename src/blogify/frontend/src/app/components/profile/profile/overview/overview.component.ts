import { Component, OnInit } from '@angular/core';
import { Article } from "../../../../models/Article";
import { ArticleService } from "../../../../services/article/article.service";
import { AuthService } from '../../../../shared/auth/auth.service';
import { ActivatedRoute, Params } from '@angular/router';
import { User } from '../../../../models/User';

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {

    articles: Article[] = [];
    forUser: User;

    constructor (
        private articleService: ArticleService,
        private authService: AuthService,
        private route: ActivatedRoute
    ) {}

    ngOnInit() {
        this.route.parent.params.subscribe((params: Params) => {
            let username = params['username'];

            this.articleService.getArticleByForUser (
                username,
                ['title', 'summary', 'createdBy', 'categories', 'createdAt', 'likeCount', 'commentCount']
            ).then(articles => {
                this.articles = articles
            });

            this.authService.getByUsername(username).then(user => {{
                this.forUser = user;
            }})
        })
    }

}
