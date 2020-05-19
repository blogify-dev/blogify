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

    draftState: { available: Article[], areShowing: boolean, isEditing: Article | null } =
        { available: [], areShowing: false, isEditing: null }

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
            tap(state => this.draftState.areShowing = state)
        ).subscribe();

        // This only collects new navigation events. not the initial ones. So this takes care of in-visit navigation. -- Ben
        this.router.events.pipe (
            filter(e => e instanceof NavigationEnd),
            map(e => (e as NavigationEnd).url.split('/').some(it => it === 'drafts')),
            tap(state => this.draftState.areShowing = state)
        ).subscribe();

        // Get all drafts for user if logged in
        this.authService.observeIsLoggedIn().pipe (
            filter(state => state),
            tap(() => {
                this.articleService.queryArticleDraftsForUser ({
                    quantity: 15,
                    page: 0,
                    byUser: this.authService.currentUser,
                    fields: ['title', 'summary', 'content', 'categories', 'createdBy', 'createdAt', 'isDraft']
                }).then(it => this.draftState.available = it.data);
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
            this.result = {
                status: 'success',
                message: `${asDraft ? 'Draft' : 'Article'} created successfully`
            };

            if (!asDraft)
                await this.router.navigateByUrl(`/article/${article['uuid']}`);
            else this.draftState.available.push(article as Article);
        }).catch(() => {
            this.result = {
                status: 'error',
                message: `Error while creating ${asDraft ? 'article' : 'draft'}`
            };
        });
    }

    saveOrUpdateDraft() {
        if (this.draftState.isEditing) {
            this.draftState.isEditing = {
                ...this.form.value,
                uuid: this.draftState.isEditing.uuid,
                categories: this.formCategories.controls.map(control => ({ name: control.value }))
            };

            this.articleService.updateArticle(this.draftState.isEditing)
                .then(() => this.result = { status: 'success', message: 'Draft updated successfully' })
                .catch(() => this.result = { status: 'error', message: 'Error while updating draft' });
        } else {
            this.createNewArticle(true);
        }
    }

    deleteDraft(uuid: string) {
        this.articleService.deleteArticle(uuid)
            .then(() => {
                this.draftState.available.splice(this.draftState.available.findIndex(it => it.uuid === uuid), 1);
                this.draftState.isEditing = null;
            });
    }

    useDraft(draft: Article) {
        this.formCategories.clear();

        this.form.patchValue ({
            title: draft.title,
            content: draft.content,
            summary: draft.summary,
        });

        draft.categories.forEach(cat => this.formCategories.push(new FormControl(cat.name)));

        this.draftState.isEditing = draft;

        this.router.navigateByUrl('/article/new').then();
    }

}
