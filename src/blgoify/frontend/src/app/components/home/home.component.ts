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



ngOnInit(): void {
}



}
