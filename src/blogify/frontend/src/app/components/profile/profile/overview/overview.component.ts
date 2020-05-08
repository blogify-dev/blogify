import { Component, OnInit } from '@angular/core';
import { Article } from '@blogify/models/Article';
import { ListingQuery } from '@blogify/models/ListingQuery';
import { ArticleService } from '@blogify/core/services/article/article.service';
import { ActivatedRoute, Params } from '@angular/router';
import { User } from '@blogify/models/User';
import { Shadow } from '@blogify/models/Shadow';
import { UserService } from '@blogify/shared/services/user-service/user.service';

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss'],
})
export class OverviewComponent implements OnInit {

    forUser: User
    listing: ListingQuery<Article> & { byUser?: Shadow<User> }

    constructor (
        private articleService: ArticleService,
        private userService: UserService,
        private route: ActivatedRoute,
    ) {}

    ngOnInit() {
        this.route.parent.params.subscribe((params: Params) => {
            const username = params.username;

            this.userService.getByUsername(username).then(user => {
                {
                    this.forUser = user;
                    this.listing = { ...new ListingQuery(15, 0), byUser: this.forUser };
                }
            });
        });
    }

}
