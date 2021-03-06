import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MainProfileComponent } from './main/main-profile.component';
import { OverviewComponent } from './overview/overview.component';
import { SettingsComponent } from './settings/settings.component';
import { FollowsComponent } from "./follows/follows.component";
import { ManageComponent } from "./manage/manage.component";

const routes: Routes = [
    {
        path: 'profile/:username', component: MainProfileComponent,
        children: [
            { path: '', redirectTo: 'overview', pathMatch: 'full' },
            { path: 'overview', component: OverviewComponent, },
            { path: 'friends', component: FollowsComponent, },
            { path: 'settings', component: SettingsComponent, },
            { path: 'manage', component: ManageComponent }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ProfileRoutingModule { }
