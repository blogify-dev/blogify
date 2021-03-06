import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TabHeaderComponent } from '@blogify/shared/components/tab-header/tab-header.component';
import { ProfileRoutingModule } from '@blogify/profiles/profile-routing.module';
import { ProfilePictureComponent } from '@blogify/shared/components/profile-picture/profile-picture.component';
import { ShowAllArticlesComponent } from '@blogify/shared/components/show-all-articles/show-all-articles.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { RelativeTimePipe } from '@blogify/shared/relative-time/relative-time.pipe';
import { UserDisplayComponent } from '@blogify/shared/components/user-display/user-display.component';
import { DarkThemeDirective } from '@blogify/shared/directives/dark-theme/dark-theme.directive';
import { CompactDirective } from '@blogify/shared/directives/compact/compact.directive';
import { FormsModule } from '@angular/forms';
import { SingleArticleBoxComponent } from '@blogify/shared/components/show-all-articles/single-article-box/single-article-box.component';
import { FilteringMenuComponent } from '@blogify/shared/components/show-all-articles/filtering-menu/filtering-menu.component';
import { AdminRoutingModule } from '@blogify/core/components/admin/admin/admin-routing.module';
import { ShowAllUsersComponent } from '@blogify/shared/components/show-all-users/show-all-users.component';
import { SingleUserBoxComponent } from '@blogify/shared/components/show-all-users/single-user-box/single-user-box.component';
import { NotificationComponent } from '@blogify/shared/components/notification/notification.component';

@NgModule({
    declarations: [
        RelativeTimePipe,
        DarkThemeDirective,
        CompactDirective,
        TabHeaderComponent,
        ProfilePictureComponent,
        ShowAllArticlesComponent,
        UserDisplayComponent,
        SingleArticleBoxComponent,
        FilteringMenuComponent,
        ShowAllUsersComponent,
        SingleUserBoxComponent,
        NotificationComponent,
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
        ShowAllArticlesComponent,
        UserDisplayComponent,
        ShowAllUsersComponent,
        NotificationComponent
    ]
})
export class SharedModule { }
