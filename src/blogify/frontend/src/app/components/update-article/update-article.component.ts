import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { faHeart } from '@fortawesome/free-regular-svg-icons';
import { Subscription } from 'rxjs';
import { Article, Category } from '../../models/Article';
import { User } from '../../models/User';
import { ArticleService } from '../../services/article/article.service';
import { AuthService } from '../../shared/auth/auth.service';

type Result = 'none' | 'success' | 'error';

@Component({
    selector: 'app-update-article',
    templateUrl: './update-article.component.html',
    styleUrls: ['./update-article.component.scss']
})
export class UpdateArticleComponent implements OnInit {

    constructor (
        private activatedRoute: ActivatedRoute,
        private articleService: ArticleService,
        private http: HttpClient,
        private router: Router,
        private authService: AuthService,
    ) {}

    faHeartOutline = faHeart;

    routeMapSubscription: Subscription;
    article: Article;
    user: User;
    validations: object;

    result: { status: Result, message: string } = { status: 'none', message: null };

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
        title:      this.formTitle,
        summary:    this.formSummary,
        content:    this.formContent,
        categories: this.formCategories
    });

    async ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const articleUUID = map.get('uuid');

            this.article = await this.articleService.fetchOrGetArticle (articleUUID,);

            this.article.categories.forEach((cat: Category) => this.formCategories.push(new FormControl(cat.name)));

            this.form.setValue({
                categories: this.article.categories.map(it => it.name),
                title: this.article.title,
                summary: this.article.summary,
                content: this.article.content
            });
        });

        this.authService.observeIsLoggedIn().subscribe(state => {
            if (state) this.authService.userProfile.then(it => this.user = it);
            else console.error('[blogifyNewArticle] must be logged in; check links to not allow unauth access to new-article');
        });

        this.validations = await this.http.get<object>('/api/articles/_validations').toPromise();
    }

    // noinspection DuplicatedCode
    private validateOnServer(fieldName: string): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (this.validations === undefined) return null; // Validations are not ready; assume valid
            const validationRegex = this.validations[fieldName];
            if (validationRegex === undefined) return null; // No validation for the field; assume valid

            const matchResult = (control.value as string).match(validationRegex); // Match validation against value

            // tslint:disable-next-line:max-line-length
            if (matchResult !== null) { return null; } else return { reason: `doesn't match regex '${validationRegex.source}'` }; // No matches; invalid
        };
    }

    // noinspection JSMethodCanBeStatic
    // noinspection DuplicatedCode

    transformArticleData(form: FormGroup): object {
        const article: Article = form.value;
        article.uuid = this.article.uuid;
        article.categories = (form.controls.categories as FormArray).controls.map(it => it.value as string)
            // .filter((cat: string) => cat.match(/\\s/) !== null)
            .map(cat => ({ name: cat }));
        article.createdBy = this.article.createdBy;
        console.table(article);
        return article;
    }


    async updateArticle() {
        await this.articleService.updateArticle (
            (this.transformArticleData(this.form) as Article)
        ).then(async () => {
            await this.router.navigateByUrl(`/article/${(this.article.uuid)}`);
        }).catch(() =>
            this.result = { status: 'error', message: 'Error while creating article' }
        );
    }

    addCategory(input: HTMLInputElement) {
        if (!input.value.match(/^\s*$/)) {
            this.formCategories.push(new FormControl(input.value.trim()));
            input.value = '';
        }
    }

}
