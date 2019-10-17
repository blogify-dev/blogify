import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../models/Article';
import { AuthService } from '../../services/auth/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {

    @Input() title = 'Articles';
    @Input() articles: Article[];

    constructor(
        private authService: AuthService,
        private router: Router
    ) {}

    ngOnInit() {

    }

    async navigateToNewArticle() {
        if (this.authService.userToken === '') {
            const url = `/login?redirect=/new-article`;
            console.log(url);
            await this.router.navigateByUrl(url);
        } else {
            await this.router.navigateByUrl('/new-article');
        }
    }

}
