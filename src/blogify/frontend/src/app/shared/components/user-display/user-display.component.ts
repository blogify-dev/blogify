import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { User } from '../../../models/User';

@Component({
    selector: 'app-user-display',
    templateUrl: './user-display.component.html',
    styleUrls: ['./user-display.component.scss']
})
export class UserDisplayComponent implements OnInit, OnChanges {

    @Input() user: User;
    @Input() info: 'username' | 'name' = 'username';
    @Input() showSecondaryInfo: boolean;
    @Input() emSize: number = 3;
    @Input() sizeRatio = 2.4;
    @Input() displayedVertically: boolean = false;

    infoText: string;
    secondaryInfoText: string;

    constructor() {}

    ngOnInit() {
        this.infoText = this.info === 'username' ? this.user.username : this.user.name;
        this.secondaryInfoText = this.info === 'username' ? this.user.name : `@${this.user.username}`;
    }

    ngOnChanges(changes: SimpleChanges): void {
        this.infoText = this.info === 'username' ? this.user.username : this.user.name;
        this.secondaryInfoText = this.info === 'username' ? this.user.name : `@${this.user.username}`;
    }

}
