import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from 'src/app/shared/auth/auth.service';
import { User } from 'src/app/models/User';
import { Article } from '../../models/Article';
import { ArticleService } from '../../services/article/article.service';
import { Tab, TabList } from '../../shared/components/tab-header/tab-header.component';
import { MainProfileComponent } from './profile/main/main-profile.component';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {

    routeMapSubscription: Subscription;
    user: User;

    constructor (
        private activatedRoute: ActivatedRoute,
        private authService: AuthService,
    ) {}

    async ngOnInit() {
        this.routeMapSubscription = this.activatedRoute.paramMap.subscribe(async (map) => {
            const username = map.get('username');

            this.user = await this.authService.getByUsername(username);
        });
    }

    ngOnDestroy() {
        this.routeMapSubscription.unsubscribe();
    }

}
