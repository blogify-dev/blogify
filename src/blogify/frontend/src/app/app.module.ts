import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ContenteditableValueAccessorModule } from '@tinkoff/angular-contenteditable-accessor';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './components/login/login.component';
import { ProfileComponent } from './components/profile/profile.component';
import { HomeComponent } from './components/home/home.component';
import { RouterModule } from '@angular/router';
import { NewArticleComponent } from './components/newarticle/new-article.component';
import { ShowArticleComponent } from './components/show-article/show-article.component';
import { ArticleCommentsComponent } from './components/comment/article-comments.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { SingleCommentComponent } from './components/comment/single-comment/single-comment.component';
import { CreateCommentComponent } from './components/comment/create-comment/create-comment.component';
import { UpdateArticleComponent } from './components/update-article/update-article.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { MarkdownModule } from 'ngx-markdown';
import { SharedModule } from './shared/shared.module';
import { ProfileModule } from '@blogify/profiles/profile.module';
import { FooterComponent } from './components/footer/footer.component';
import { ClipboardModule } from 'ngx-clipboard';
import { AdminComponent } from './components/admin/admin.component';
import { AdminModule } from './components/admin/admin/admin.module';
import { FollowsComponent } from '@blogify/profiles/follows/follows.component';
import { UsersComponent } from './components/users/users.component';
import { ProfileSlideoverComponent } from './components/profile-slideover/profile-slideover.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NotificationComponent } from './shared/components/notification/notification.component';
import { PushService } from './shared/services/push/push.service';
import { ToastContainerModule, ToastrModule } from 'ngx-toastr';
import { NotificationsPopoverComponent } from './components/navbar/notifications-popover/notifications-popover.component';
import { AuthService } from './shared/services/auth/auth.service';
import { PageNotFoundFallbackComponent } from './components/page-not-found-fallback/page-not-found-fallback.component';

@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        ProfileComponent,
        HomeComponent,
        NewArticleComponent,
        ShowArticleComponent,
        ArticleCommentsComponent,
        NavbarComponent,
        SingleCommentComponent,
        CreateCommentComponent,
        UpdateArticleComponent,
        FooterComponent,
        AdminComponent,
        FollowsComponent,
        UsersComponent,
        ProfileSlideoverComponent,
        NotificationsPopoverComponent,
        PageNotFoundFallbackComponent,
    ],
    imports: [
        BrowserModule,
        RouterModule,
        AppRoutingModule,
        HttpClientModule,
        FormsModule,
        ReactiveFormsModule,
        ContenteditableValueAccessorModule,
        FontAwesomeModule,
        MarkdownModule.forRoot(),
        ToastContainerModule,
        ClipboardModule,
        SharedModule,
        ProfileModule,
        AdminModule,
        BrowserAnimationsModule,
        ToastrModule.forRoot({
            toastClass: 'toast',
            toastComponent: NotificationComponent,
            positionClass: 'toast-bottom-right'
        }),
        ToastContainerModule
    ],
    providers: [],
    exports: [],
    bootstrap: [AppComponent]
})
export class AppModule {
    // noinspection JSUnusedLocalSymbols
    constructor(private pushService: PushService, private authService: AuthService) {
        this.authService.setupAndPopulateCache();
    }
}
