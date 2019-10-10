import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ArticleService } from '../../services/article/article.service';
import { Article } from '../../models/Article';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-update-article',
    templateUrl: './update-article.component.html',
    styleUrls: ['./update-article.component.scss']
})
export class UpdateArticleComponent implements OnInit {

    routeMapSubscription: Subscription;
    article: Article;

    constructor(
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
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
    }

}
