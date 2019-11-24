import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { ProfileComponent } from './components/profile/profile.component';
import { NewArticleComponent } from './components/newarticle/new-article.component';
import { ShowArticleComponent } from './components/show-article/show-article.component';
import { UpdateArticleComponent } from './components/update-article/update-article.component';
import { AdminComponent } from './components/admin/admin.component';
import { Error404FallbackComponent } from "./components/error404-fallback/error404-fallback.component";


const routes: Routes = [
    { path: 'home', component: HomeComponent },
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: LoginComponent },
    { path: 'article/new', component: NewArticleComponent },
    /*{ path: 'profile/**', component: ProfileComponent },*/
    { path: 'article/:uuid', component: ShowArticleComponent },
    { path: 'article/update/:uuid', component: UpdateArticleComponent },
    /*{ path: 'admin/**', component: AdminComponent },*/
    /*{ path: '**', component: Error404FallbackComponent },*/
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
