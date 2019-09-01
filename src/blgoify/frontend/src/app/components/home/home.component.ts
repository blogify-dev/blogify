import { Component, OnInit } from '@angular/core';
import { ArticleService } from 'src/app/services/article/article.service';
import { Article } from '../../models/Article'
import { Router } from '@angular/router';
import { LoginComponent } from "../login/login.component";


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  title = 'blogify';

  articles: Article[];


  constructor(private articleService: ArticleService, private router: Router) {
  }

ngOnInit(): void {
}

  toLoginPage(){
    this.router.navigateByUrl('/auth/signin').then(r => Promise);
  }

  toRegisterPage(){
    this.router.navigateByUrl('/auth/signup').then(r => Promise);
  }

  getAllArticles() {
    this.articleService.getAllArticles().subscribe((it) => {
        this.articles = it;
        console.log(this.articles)
    })

  }

  createNewArticle() {
    return this.articleService.getAllArticles()
  }

}
