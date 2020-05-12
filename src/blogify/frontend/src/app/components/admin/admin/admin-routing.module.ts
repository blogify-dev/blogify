import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { MainAdminComponent } from '@blogify/core/components/admin/admin/main/main-admin.component';
import { OverviewComponent } from '@blogify/core/components/admin/admin/overview/overview.component';

const routes: Routes = [
    {
        path: 'admin', component: MainAdminComponent,
        children: [
            { path: '', redirectTo: 'overview', pathMatch: 'full' },
            { path: 'overview', component: OverviewComponent }
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AdminRoutingModule { }
