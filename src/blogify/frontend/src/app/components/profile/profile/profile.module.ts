import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProfileRoutingModule } from './profile-routing.module';
import { SettingsComponent } from './settings/settings.component';
import { MainProfileComponent } from './main/main-profile.component';
import { OverviewComponent } from './overview/overview.component';
import { SharedModule } from '../../../shared/shared.module';
import { CoverPictureComponent } from './cover-picture/cover-picture.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { FormsModule } from "@angular/forms";
import { ManageComponent } from './manage/manage.component';

@NgModule({
    declarations: [
        SettingsComponent,
        MainProfileComponent,
        OverviewComponent,
        CoverPictureComponent,
        ManageComponent
    ],
    exports: [
        MainProfileComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        ProfileRoutingModule,
        FontAwesomeModule,
        FormsModule
    ]
})

export class ProfileModule { }
