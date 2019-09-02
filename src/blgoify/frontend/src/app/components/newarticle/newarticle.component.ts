import { Component, OnInit } from '@angular/core';
import {Article} from "../../models/Article";
import {ArticleService} from "../../services/article/article.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-newarticle',
  templateUrl: './newarticle.component.html',
  styleUrls: ['./newarticle.component.scss']
})
export class NewarticleComponent implements OnInit {


  articles: Article;
  token: string;
  uuid: string;


  constructor(private articleService: ArticleService, private router: Router) {
  }

  createNewArticle() {
    return this.articleService.createNewArticle(this.articles, this.token).toPromise();
  }

  updateArticle(){
    return this.articleService.updateArticle(this.uuid, this.articles, this.token).toPromise();

  }

  getAllArticles() {
    this.articleService.getAllArticles().subscribe((it: any) => {
      this.articles = it;
      console.log(this.articles)
    })

  }
  ngOnInit() {
  }

}
