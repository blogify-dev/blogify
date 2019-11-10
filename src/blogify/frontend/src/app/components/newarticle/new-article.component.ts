import { Component, OnInit } from '@angular/core';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { User } from '../../models/User';
import { StaticFile } from "../../models/Static";
import { AuthService } from '../../shared/auth/auth.service';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

type Result = 'none' | 'success' | 'error';

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
    validations: object;

    result: { status: Result, message: string } = { status: 'none', message: null };

    constructor (
        private articleService: ArticleService,
        private authService: AuthService,
        private http: HttpClient
    ) {}

    async ngOnInit() {
        this.user = await this.authService.userProfile;
        this.validations = await this.http.get<object>('/api/articles/_validations').toPromise();
        console.warn(this.validations);
    }

    private validateOnServer(fieldName: string): ValidatorFn {
        const this_ = this;
        return function (control: AbstractControl): ValidationErrors | null {
            if (this_.validations === undefined) return null; // Validations are not ready; assume valid
            const validationRegex = this_.validations[fieldName];
            if (validationRegex === undefined) return null; // No validation for the field; assume valid

            const matchResult = (<string> control.value).match(validationRegex); // Match validation against value

            if (matchResult !== null) { return null; } // Any matches; valid
            else return { reason: `doesn't match regex '${validationRegex.source}'` }; // No matches; invalid
        }
    }

    formTitle = new FormControl('',
        [Validators.required, this.validateOnServer('title')]
    );
    formSummary = new FormControl('',
        [Validators.required, this.validateOnServer('summary')]
    );
    formContent = new FormControl('',
        [Validators.required, this.validateOnServer('content')]
    );
    formCategories = new FormArray([new FormControl('')]);

    form = new FormGroup({
        'title':      this.formTitle,
        'summary':    this.formSummary,
        'content':    this.formContent,
        'categories': this.formCategories
    });

    transformArticleData(input: object): object {
        input['categories'] = input['categories'].map(cat => { return { name: cat }});
        return input
    }

    createNewArticle() {
        this.articleService.createNewArticle (
            (<Article> this.transformArticleData(this.form.value))
        ).then(_ =>
            this.result = { status: 'success', message: 'Article created successfuly' }
        ).catch(_ =>
            this.result = { status: 'error', message: 'Error while creating article' }
        );
    }

    addCategory() {
        this.formCategories.push(new FormControl(''));
    }

}
