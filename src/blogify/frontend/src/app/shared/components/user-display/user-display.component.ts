import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { User } from '@blogify/models/User';

@Component({
    selector: 'app-user-display',
    templateUrl: './user-display.component.html',
    styleUrls: ['./user-display.component.scss']
})
export class UserDisplayComponent implements OnInit, OnChanges {

    @Input() user: User;
    @Input() info: 'username' | 'name' = 'username';
    @Input() showSecondaryInfo: boolean;
    @Input() emSize = 3;
    @Input() sizeRatio = 0.75;
    @Input() displayedVertically = false;

    infoText: string;
    secondaryInfoText: string;

    constructor() {}

    ngOnInit() {
        if (this.user) {
            this.infoText = this.info === 'username' ? this.user.username : this.user.name;
            this.secondaryInfoText = this.info === 'username' ? this.user.name : `@${this.user.username}`;
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.user) {
            this.infoText = this.info === 'username' ? this.user.username : this.user.name;
            this.secondaryInfoText = this.info === 'username' ? this.user.name : `@${this.user.username}`;
        }
    }

}
