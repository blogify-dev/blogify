import { Component, OnInit } from '@angular/core';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { User } from '../../models/User';
import { StaticFile } from "../../models/Static";
import { AuthService } from '../../shared/auth/auth.service';
import {AbstractControl, FormArray, FormControl, FormGroup, Validators} from '@angular/forms';
import {DatePipe} from '@angular/common';

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {

    article: Article = {
        uuid: '',
        title: '',
        categories: [],
        content: '',
        summary: '',
        createdBy: new User('', '', '', '', new StaticFile('-1')),
        createdAt: Date.now(),
    };
    user: User;

    datePipe = new DatePipe('en-US');

    formTitle =      new FormControl('', [Validators.required]);
    formSummary =    new FormControl('', [Validators.required]);
    formContent =    new FormControl('', [Validators.required]);
    formCategories = new FormArray([new FormControl('')]);

    form = new FormGroup({
        'title':      this.formTitle,
        'summary':    this.formSummary,
        'content':    this.formContent,
        'categories': this.formCategories
    });

    constructor(private articleService: ArticleService, private authService: AuthService) {}

    async ngOnInit() {
        this.user = await this.authService.userProfile;
    }

    transformArticleData(input: object): object {
        input['categories'] = input['categories'].map(cat => { return { name: cat }});
        return input
    }

    createNewArticle() {
        alert('a');
        this.articleService.createNewArticle (
            (<Article> this.transformArticleData(this.form.value))
        ).then(article =>
            console.warn(article)
        );
    }

    addCategory() {
        this.formCategories.push(new FormControl(''));
    }

}
