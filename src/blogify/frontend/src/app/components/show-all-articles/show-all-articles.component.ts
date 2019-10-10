import { Component, Input, OnInit} from '@angular/core';
import { Article} from "../../models/Article";
import { AuthService} from "../../services/auth/auth.service";
import { Router} from "@angular/router";

@Component({
    selector: 'app-show-all-articles',
    templateUrl: './show-all-articles.component.html',
    styleUrls: ['./show-all-articles.component.scss']
})
export class ShowAllArticlesComponent implements OnInit {

    @Input() title:    string = "Articles";
    @Input() articles: Article[];

    constructor(
        private authService: AuthService,
        private router: Router
    ) {}

    ngOnInit() {

    }

    async navigateToNewArticle() {
        if (this.authService.userToken == '') {
            await this.router.navigateByUrl('/login')
        } else {
            await this.router.navigateByUrl('/new-article')
        }
    }

}
