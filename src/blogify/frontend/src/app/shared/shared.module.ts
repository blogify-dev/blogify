import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabHeaderComponent } from './components/tab-header/tab-header.component';
import { ProfileRoutingModule } from '../components/profile/profile/profile-routing.module';
import { ProfilePictureComponent } from './components/profile-picture/profile-picture.component';
import { ShowAllArticlesComponent } from './components/show-all-articles/show-all-articles.component';
import { FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import { RelativeTimePipe } from './relative-time/relative-time.pipe';
import { UserDisplayComponent } from './components/user-display/user-display.component';
import { DarkThemeDirective } from './directives/dark-theme/dark-theme.directive';
import { CompactDirective } from './directives/compact/compact.directive';

@NgModule({
    declarations: [
        RelativeTimePipe,
        DarkThemeDirective,
        CompactDirective,
        TabHeaderComponent,
        ProfilePictureComponent,
        ShowAllArticlesComponent,
        UserDisplayComponent
    ],
    imports: [
        CommonModule,
        ProfileRoutingModule,
        FontAwesomeModule
    ],
    exports: [
        RelativeTimePipe,
        DarkThemeDirective,
        CompactDirective,
        TabHeaderComponent,
        ProfilePictureComponent,
        ShowAllArticlesComponent,
        UserDisplayComponent
    ]
})
export class SharedModule { }
