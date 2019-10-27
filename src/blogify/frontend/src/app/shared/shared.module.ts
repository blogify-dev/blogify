import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabHeaderComponent } from './components/tab-header/tab-header.component';
import { ProfileRoutingModule } from '../components/profile/profile/profile-routing.module';
import { ProfilePictureComponent } from './components/profile-picture/profile-picture.component';
import { ShowAllArticlesComponent } from './components/show-all-articles/show-all-articles.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {RelativeTimePipe} from './relative-time/relative-time.pipe';

@NgModule({
    declarations: [
        RelativeTimePipe,
        TabHeaderComponent,
        ProfilePictureComponent,
        ShowAllArticlesComponent
    ],
    imports: [
        CommonModule,
        ProfileRoutingModule,
        FontAwesomeModule
    ],
    exports: [
        TabHeaderComponent,
        ProfilePictureComponent,
        ShowAllArticlesComponent,
        RelativeTimePipe
    ]
})
export class SharedModule { }
