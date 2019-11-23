import { Component, OnInit, ViewChild } from '@angular/core';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { User } from '../../models/User';
import { StaticFile } from '../../models/Static';
import { AuthService } from '../../shared/auth/auth.service';
import { AbstractControl, FormArray, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { faExclamationCircle, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';
import { ToasterComponent } from '../../shared/components/toaster/toaster.component';
import { ToasterService } from '../../shared/services/toaster/toaster.service';
import { Toast, ToastStyle } from '../../shared/services/toaster/models/Toast';

type Result = 'none' | 'success' | 'error';

@Component({
    selector: 'app-new-article',
    templateUrl: './new-article.component.html',
    styleUrls: ['./new-article.component.scss']
})
export class NewArticleComponent implements OnInit {

    faPlus = faPlus;

    article: Article = {
        uuid: '',
        title: '',
        categories: [],
        content: '',
        summary: '',
        createdBy: new User('', '', '', '', new StaticFile('-1')),
        createdAt: Date.now(),
        numberOfComments: 0,
    };

    user: User;
    validations: object;

    result: { status: Result, message: string } = { status: 'none', message: null };

    @ViewChild(ToasterComponent, { static: false })
    private toaster: ToasterComponent;

    constructor (
        private articleService: ArticleService,
        private authService: AuthService,
        private toasterService: ToasterService,
        private http: HttpClient,
        private router: Router,
    ) {}

    async ngOnInit() {
        this.user = await this.authService.userProfile;
        this.validations = await this.http.get<object>('/api/articles/_validations').toPromise();

        this.toasterService.plugInto(this.toaster);
        this.toasterService.feed (
            new Toast ({
                header: 'One toast !',
                content: 'Body of the first toast, neutral colored ! :)',
                backgroundColor: ToastStyle.NEUTRAL
            }),
            new Toast ({
                header: 'The Second Toast...',
                content: 'Contents of the second toast. Interesting.',
                icon: faExclamationCircle,
                backgroundColor: ToastStyle.MILD
            }),
            new Toast ({
                header: 'A THIRD ONE !',
                content: 'Danger danger danger danger danger danger danger !',
                icon: faTimes,
                backgroundColor: ToastStyle.NEGATIVE
            })
        );
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

    // noinspection JSMethodCanBeStatic
    transformArticleData(input: object): object {
        input['categories'] = input['categories'].map(cat => { return { name: cat }});
        return input
    }

    createNewArticle() {
        this.articleService.createNewArticle (
            (<Article> this.transformArticleData(this.form.value))
        ).then(async uuid => {
            this.result = { status: 'success', message: 'Article created successfully' };
            await this.router.navigateByUrl(`/article/${uuid}`)
        }).catch(() =>
            this.result = { status: 'error', message: 'Error while creating article' }
        );
    }

    addCategory() {
        this.formCategories.push(new FormControl(''));
    }

}
