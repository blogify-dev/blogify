import { Component, OnInit } from '@angular/core';
import { Article } from '../../../../models/Article';
import { ListingQuery } from '../../../../models/ListingQuery';
import { ArticleService } from '../../../../services/article/article.service';
import { AuthService } from '../../../../shared/auth/auth.service';
import { ActivatedRoute, Params } from '@angular/router';
import { User } from '../../../../models/User';

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {

    forUser: User;
    listing: ListingQuery<Article>;

    constructor (
        private articleService: ArticleService,
        private authService: AuthService,
        private route: ActivatedRoute
    ) {}

    ngOnInit() {
        this.route.parent.params.subscribe((params: Params) => {
            const username = params.username;

            this.authService.getByUsername(username).then(user => {{
                this.forUser = user;
                this.listing = new ListingQuery(15, 0);
            }});
        });
    }

}
