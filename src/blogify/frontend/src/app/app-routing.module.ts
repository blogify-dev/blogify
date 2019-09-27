import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { ProfileComponent } from './components/profile/profile.component';
import {SearchComponent} from "./components/search/search.component";
import {NewArticleComponent} from "./components/newarticle/new-article.component";
import { ShowArticleComponent } from "./components/show-article/show-article.component";


const routes: Routes = [
    { path: 'home', component: HomeComponent },
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: LoginComponent },
    { path: 'search', component: SearchComponent },
    { path: 'new-article', component: NewArticleComponent },
    { path: 'profile/:uuid', component: ProfileComponent },
    { path: 'article/:uuid', component: ShowArticleComponent },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
