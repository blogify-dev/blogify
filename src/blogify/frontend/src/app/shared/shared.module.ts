import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabHeaderComponent } from '@blogify/shared/components/tab-header/tab-header.component';
import { ProfileRoutingModule } from '@blogify/profiles/profile-routing.module';
import { ProfilePictureComponent } from '@blogify/shared/components/profile-picture/profile-picture.component';
import { ContentFeedComponent } from '@blogify/shared/components/content-feed/content-feed.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RelativeTimePipe } from '@blogify/shared/relative-time/relative-time.pipe';
import { UserDisplayComponent } from '@blogify/shared/components/user-display/user-display.component';
import { DarkThemeDirective } from '@blogify/shared/directives/dark-theme/dark-theme.directive';
import { CompactDirective } from '@blogify/shared/directives/compact/compact.directive';
import { FormsModule } from '@angular/forms';
import { SingleArticleBoxComponent } from '@blogify/shared/components/content-feed/single-article-box/single-article-box.component';
import { AdminRoutingModule } from '@blogify/core/components/admin/admin/admin-routing.module';
import { ShowAllUsersComponent } from '@blogify/shared/components/show-all-users/show-all-users.component';
import { SingleUserBoxComponent } from '@blogify/shared/components/show-all-users/single-user-box/single-user-box.component';
import { NotificationComponent } from '@blogify/shared/components/notification/notification.component';
import { ContentHostDirective } from './directives/content-host/content-host.directive';

@NgModule({
    declarations: [
        RelativeTimePipe,
        DarkThemeDirective,
        CompactDirective,
        TabHeaderComponent,
        ProfilePictureComponent,
        ContentFeedComponent,
        UserDisplayComponent,
        SingleArticleBoxComponent,
        ShowAllUsersComponent,
        SingleUserBoxComponent,
        NotificationComponent,
        ContentHostDirective,
    ],
    imports: [
        CommonModule,
        ProfileRoutingModule,
        AdminRoutingModule,
        FontAwesomeModule,
        FormsModule,
    ],
    exports: [
        RelativeTimePipe,
        DarkThemeDirective,
        CompactDirective,
        TabHeaderComponent,
        ProfilePictureComponent,
        ContentFeedComponent,
        UserDisplayComponent,
        ShowAllUsersComponent,
        NotificationComponent
    ]
})
export class SharedModule { }
