import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { ProfileComponent } from './components/profile/profile.component';
import { NewArticleComponent } from './components/newarticle/new-article.component';
import { ShowArticleComponent } from './components/show-article/show-article.component';
import { UpdateArticleComponent } from './components/update-article/update-article.component';
import {UpdateUserComponent} from "./components/update-user/update-user.component";


const routes: Routes = [
    { path: 'home', component: HomeComponent },
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: LoginComponent },
    { path: 'new-article', component: NewArticleComponent },
    { path: 'profile/**', component: ProfileComponent },
    { path: 'article/:uuid', component: ShowArticleComponent },
    { path: 'article/update/:uuid', component: UpdateArticleComponent },
    { path: 'user/update', component: UpdateUserComponent },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
