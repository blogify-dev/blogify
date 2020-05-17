import { Component, OnInit } from '@angular/core';
import { Article } from '@blogify/models/Article';
import { ArticleService } from '@blogify/core/services/article/article.service';
import { User } from '@blogify/models/User';
import { AuthService } from '@blogify/shared/services/auth/auth.service';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { faArrowLeft, faDraftingCompass } from '@fortawesome/free-solid-svg-icons';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { faTrashAlt } from '@fortawesome/free-regular-svg-icons';
import { filter, map, tap } from 'rxjs/operators';

type Result = 'none' | 'success' | 'error';

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {

    faDraftingCompass = faDraftingCompass;

    faArrowLeft = faArrowLeft;
    faTrashAlt = faTrashAlt;

    user: User;
    validations: object; // TODO Make this strongly-typed -- Ben

    result: { status: Result, message: string } = { status: 'none', message: null };

    drafts: Article[] = [];
    showingDrafts = false

    constructor (
        private articleService: ArticleService,
        private authService: AuthService,
        private http: HttpClient,
        private activatedRoute: ActivatedRoute,
        public router: Router,
    ) {}

    async ngOnInit() {
        // This needs to go. TODO implement route guards. -- Ben
        this.authService.observeIsLoggedIn().subscribe(state => {
            if (state) this.user = this.authService.currentUser;
            else console.error('[blogifyNewArticle] must be logged in; check links to not allow unauth access to new-article');
        });

        // We need to listen to `activatedRoute.firstChild.url`, but that seems to only exist
        // on an initial page load for some reason. -- Ben
        this.activatedRoute.firstChild?.url.pipe (
            map(fragments => fragments.some(it => it.path === 'drafts')),
            tap(state => this.showingDrafts = state)
        ).subscribe();

        // This only collects new navigation events. not the initial ones. So this takes care of in-visit navigation. -- Ben
        this.router.events.pipe (
            filter(e => e instanceof NavigationEnd),
            tap(event => {
                const url = (event as NavigationEnd).url;

                this.showingDrafts =  url.split('/').some(it => it === 'drafts');
            })
        ).subscribe();

        // Get all drafts for user if logged in
        this.authService.observeIsLoggedIn().pipe (
            filter(state => state),
            tap(() => {
                this.articleService.queryArticleDraftsForUser ({
                    quantity: 15,
                    page: 0,
                    byUser: this.authService.currentUser
                }).then(it => this.drafts = it.data);
            })
        ).subscribe();

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

    addCategory(input: HTMLInputElement) {
        if (input.value.trim() === '') return;

        this.formCategories.push(new FormControl(input.value));
        input.value = '';
    }

    get articleData(): object {
        return {
            ...this.form.value,
            categories: (this.form.controls['categories'] as FormArray).controls
                .map(control => ({ name: control.value as string }))
        };
    }

    createNewArticle(asDraft = false) {
        this.articleService.createNewArticle (
            { ...(this.articleData as Article), isDraft: asDraft }
        ).then(async (article: object) => {
            const uuid = article['uuid'];
            this.result = { status: 'success', message: (asDraft ? 'Draft' : 'Article') + ' created successfully' };

            if (!asDraft)
                await this.router.navigateByUrl(`/article/${uuid}`);
        }).catch(() =>
            this.result = { status: 'error', message: 'Error while creating article' }
        );
    }

    saveDraft() {
        this.createNewArticle(true);
    }

    deleteDraft(uuid: string) {
        this.articleService.deleteArticle(uuid)
            .then(() => this.drafts.splice(this.drafts.findIndex(it => it.uuid === uuid)));
    }

    useDraft(draft: Article) {
        this.formCategories.clear();
        this.form.patchValue({
            title: draft.title,
            content: draft.content,
            summary: draft.summary,
        });
        draft.categories.forEach(cat => this.formCategories.push(new FormControl(cat.name)));

        this.router.navigateByUrl('/article/new').then();
    }

}
