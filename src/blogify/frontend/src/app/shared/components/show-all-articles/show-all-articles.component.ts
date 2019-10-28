import { Component, Input, OnInit } from '@angular/core';
import { Article } from '../../../models/Article';
import { AuthService } from '../../auth/auth.service';
import { Router } from '@angular/router';
import { StaticContentService } from '../../../services/static/static-content.service';
import { faCommentAlt, faPencilAlt, faSearch } from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {

    faSearch = faSearch;
    faPencil = faPencilAlt;
    faCommentAlt = faCommentAlt;

    @Input() title = 'Articles';
    @Input() articles: Article[];
    @Input() allowCreate = true;

    constructor (
        private authService: AuthService,
        private staticContentService: StaticContentService,
        private router: Router
    ) {}

    ngOnInit() {}

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
