import { Component, OnInit } from '@angular/core';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { User } from '../../models/User';
import { StaticFile } from '../../models/Static';
import { AuthService } from '../../shared/auth/auth.service';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { faPlus } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';
import { ToasterService } from '../../shared/services/toaster/toaster.service';

type Result = 'none' | 'success' | 'error';

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {

    faPlus = faPlus;

    article: Article = new Article (
        '',
        '',
        '',
        '',
        new User('', '', '', '', [], new StaticFile('-1'), new StaticFile('-1')),
        Date.now(),
        []
    );

    user: User;
    validations: object;

    result: { status: Result, message: string } = { status: 'none', message: null };

    constructor (
        private articleService: ArticleService,
        private authService: AuthService,
        private toasterService: ToasterService,
        private http: HttpClient,
        private router: Router,
    ) {}

    async ngOnInit() {
        this.authService.observeIsLoggedIn().subscribe(state => {
            if (state) this.authService.userProfile.then(it => this.user = it);
            else console.error('[blogifyNewArticle] must be logged in; check links to not allow unauth access to new-article')
        });
        this.validations = await this.http.get<object>('/api/articles/_validations').toPromise();
    }

    private validateOnServer(fieldName: string): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (this.validations === undefined) return null; // Validations are not ready; assume valid
            const validationRegex = this.validations[fieldName];
            if (validationRegex === undefined) return null; // No validation for the field; assume valid

            const matchResult = (<string> control.value).match(validationRegex); // Match validation against value

            if (matchResult !== null) { return null; } // Any matches; valid
            else return { reason: `doesn't match regex '${validationRegex.source}'` }; // No matches; invalid
        }
    }

    public formTitle = new FormControl('',
        [Validators.required, this.validateOnServer('title')]
    );
    public formSummary = new FormControl('',
        [Validators.required, this.validateOnServer('summary')]
    );
    public formContent = new FormControl('',
        [Validators.required, this.validateOnServer('content')]
    );
    public formCategories = new FormArray([]);

    form = new FormGroup({
        'title':      this.formTitle,
        'summary':    this.formSummary,
        'content':    this.formContent,
        'categories': this.formCategories
    });

    // noinspection JSMethodCanBeStatic
    transformArticleData(form: FormGroup): object {
        const article = form.value;
        article['categories'] = (<FormArray>form.controls['categories']).controls.map(it => <string> it.value)
            // .filter((cat: string) => cat.match(/\\s/) !== null)
            .map(cat => { return { name: cat }});
        console.table(article);
        return article
    }

    createNewArticle() {
        this.articleService.createNewArticle (
            (<Article> this.transformArticleData(this.form))
        ).then(async (article: object) => {
            const uuid = article['uuid'];
            this.result = { status: 'success', message: 'Article created successfully' };
            await this.router.navigateByUrl(`/article/${uuid}`)
        }).catch(() =>
            this.result = { status: 'error', message: 'Error while creating article' }
        );
    }

    addCategory(input: HTMLInputElement) {
        this.formCategories.push(new FormControl(input.value));
        input.value = '';
    }

}
