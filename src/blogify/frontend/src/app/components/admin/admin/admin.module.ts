import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminRoutingModule } from './admin-routing.module';
import { MainAdminComponent } from './main/main-admin.component';
import { OverviewComponent } from './overview/overview.component';

@NgModule({
    declarations: [
        MainAdminComponent,
        OverviewComponent
    ],
    exports: [
        MainAdminComponent
    ],
    imports: [
        CommonModule,
        AdminRoutingModule
    ]
})
export class AdminModule { }
