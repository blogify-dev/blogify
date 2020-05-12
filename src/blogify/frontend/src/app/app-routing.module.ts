import { NgModule } from '@angular/core';
import { Routes, RouterModule, UrlSegment, UrlMatchResult } from '@angular/router';
import { HomeComponent } from '@blogify/core/components/home/home.component';
import { LoginComponent } from '@blogify/core/components/login/login.component';
import { NewArticleComponent } from '@blogify/core/components/new-article/new-article.component';
import { ShowArticleComponent } from '@blogify/core/components/show-article/show-article.component';
import { UpdateArticleComponent } from '@blogify/core/components/update-article/update-article.component';
import { UsersComponent } from '@blogify/core/components/users/users.component';
import { PageNotFoundFallbackComponent } from '@blogify/core/components/page-not-found-fallback/page-not-found-fallback.component';

const paths = {
    home: 'home',
    login: 'login',
    register: 'register',
    article: 'article',
    users: 'users',
};

const routes: Routes = [
    { path: paths.home, component: HomeComponent },
    { path: '', redirectTo: `/${paths.home}`, pathMatch: 'full' },
    { path: paths.login, component: LoginComponent },
    { path: paths.register, component: LoginComponent },
    {
        path: paths.article,
        children: [
            { path: 'new', component: NewArticleComponent },
            { path: ':uuid', component: ShowArticleComponent },
            { path: 'update/:uuid', component: UpdateArticleComponent },
        ]
    },
    { path: paths.users, component: UsersComponent },
    {
        matcher: (urlSegments: UrlSegment[]): UrlMatchResult => {
            const allRoutes = Object.keys(paths);

            if (urlSegments.length === 0) {
                return null;
            } else if (urlSegments[0].path.startsWith('profile')) {
                return { consumed: [urlSegments[0]], posParams: { 'username': urlSegments[1] } };
            } else if (!allRoutes.includes(urlSegments[0].path)) {
                return { consumed: urlSegments };
            } else {
                return null;
            }
        },
        component: PageNotFoundFallbackComponent,
    },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
