import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabHeaderComponent } from './components/tab-header/tab-header.component';
import { ProfileRoutingModule } from '../components/profile/profile/profile-routing.module';
import { ProfilePictureComponent } from './components/profile-picture/profile-picture.component';

@NgModule({
    declarations: [
        TabHeaderComponent,
        ProfilePictureComponent
    ],
    imports: [
        CommonModule,
        ProfileRoutingModule
    ],
    exports: [
        TabHeaderComponent,
        ProfilePictureComponent
    ]
})
export class SharedModule { }
