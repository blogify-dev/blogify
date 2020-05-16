import { Component, OnInit } from '@angular/core';
import { Article } from '@blogify/models/Article';
import { ArticleService } from '@blogify/core/services/article/article.service';
import { User } from '@blogify/models/User';
import { StaticFile } from '@blogify/models/Static';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { faPlus, faArrowLeft } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';
import { faTrashAlt } from "@fortawesome/free-regular-svg-icons";

type Result = 'none' | 'success' | 'error';

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {

    faPlus = faPlus;

    faArrowLeft = faArrowLeft;
    faTrashAlt = faTrashAlt;

    article: Article = new Article (
        '',
        '',
        '',
        '',
        new User('', '', '', '', [], false, '', { profilePicture: new StaticFile('-1'), coverPicture: new StaticFile('-1') }),
        Date.now(),
        false,
        []
    );

    user: User;
    validations: object;

    result: { status: Result, message: string } = { status: 'none', message: null };

    showingDrafts = false

    constructor (
        private articleService: ArticleService,
        private authService: AuthService,
        private http: HttpClient,
        private router: Router,
    ) {}

    async ngOnInit() {
        this.authService.observeIsLoggedIn().subscribe(state => {
            if (state) this.user = this.authService.currentUser;
            else console.error('[blogifyNewArticle] must be logged in; check links to not allow unauth access to new-article');
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
        };
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
            .map(cat => { return { name: cat };});
        console.table(article);
        return article;
    }

    createNewArticle() {
        this.articleService.createNewArticle (
            (<Article> this.transformArticleData(this.form))
        ).then(async (article: object) => {
            const uuid = article['uuid'];
            this.result = { status: 'success', message: 'Article created successfully' };
            await this.router.navigateByUrl(`/article/${uuid}`);
        }).catch(() =>
            this.result = { status: 'error', message: 'Error while creating article' }
        );
    }

    getAllDrafts(): Article[] {
        const fromLocalStorage = localStorage.getItem('drafts');
        return fromLocalStorage ? JSON.parse(fromLocalStorage) : [];
    }

    saveDraft() {
        const data = <Article> this.transformArticleData(this.form);

        const parsed = this.getAllDrafts();
        parsed.push({ ...data, createdAt: Date.now() / 1000 });
        localStorage.setItem('drafts', JSON.stringify(parsed));
    }

    deleteDraft(index: number) {
        const parsed = this.getAllDrafts();

        parsed.splice(index, 1)

        localStorage.setItem('drafts', JSON.stringify(parsed))
    }

    addCategory(input: HTMLInputElement) {
        if (input.value.trim() === '') return;

        this.formCategories.push(new FormControl(input.value));
        input.value = '';
    }

    toggleShowDrafts() {
        this.showingDrafts = !this.showingDrafts;
    }

    editDraft(draft) {
        console.log(draft);
        this.article.createdAt = draft.createdAt;
        this.formCategories.clear();
        this.form.patchValue({
            title: draft.title,
            content: draft.content,
            summary: draft.summary,
        });
        draft.categories.forEach(cat => this.formCategories.push(new FormControl(cat.name)));
        this.toggleShowDrafts();
    }
}
