import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
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
import { ProfileModule } from './components/profile/profile/profile.module';
import { FooterComponent } from './components/footer/footer.component';

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
    ],
    imports: [
        BrowserModule,
        RouterModule,
        AppRoutingModule,
        HttpClientModule,
        FormsModule,
        ReactiveFormsModule,
        FontAwesomeModule,
        MarkdownModule.forRoot(),
        ProfileModule,
        SharedModule,
    ],
    providers: [],
    exports: [],
    bootstrap: [AppComponent]
})
export class AppModule {}
