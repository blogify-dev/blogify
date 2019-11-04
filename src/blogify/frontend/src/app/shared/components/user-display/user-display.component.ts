import { Component, Input, OnInit } from '@angular/core';
import { User } from '../../../models/User';

@Component({
    selector: 'app-user-display',
    templateUrl: './user-display.component.html',
    styleUrls: ['./user-display.component.scss']
})
export class UserDisplayComponent implements OnInit {

    readonly EM_SIZE_TEXT_RATIO = 2.4;

    @Input() user: User;
    @Input() info: 'username' | 'name' = 'username';
    @Input() emSize: number = 3;

    infoText: string;

    constructor() {}

    ngOnInit() {
        this.infoText = this.info === 'username' ? this.user.username : this.user.name
    }

}
