import { Component, Input, OnInit } from '@angular/core';
import { User } from '../../../models/User';
import {StaticContentService} from '../../../services/static/static-content.service';

@Component({
    selector: 'app-user-display',
    templateUrl: './user-display.component.html',
    styleUrls: ['./user-display.component.scss']
})
export class UserDisplayComponent implements OnInit {

    readonly EM_SIZE_TEXT_RATIO = 2.4;

    @Input() user: User;
    @Input() info: 'username' | 'name' = 'username';
    @Input() showSecondaryInfo: boolean;
    @Input() emSize: number = 3;
    @Input() displayedVertically: boolean = false;

    infoText: string;
    secondaryInfoText: string;

    constructor() {}

    ngOnInit() {
        this.infoText = this.info === 'username' ? this.user.username : this.user.name;
        this.secondaryInfoText = this.info === 'username' ? this.user.name : `@${this.user.username}`;
    }

}
