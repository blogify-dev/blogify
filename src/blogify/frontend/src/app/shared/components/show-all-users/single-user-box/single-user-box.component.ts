import {Component, Input, OnInit} from '@angular/core';

@Component({
    selector: 'app-single-user-box',
    templateUrl: './single-user-box.component.html',
    styleUrls: ['./single-user-box.component.scss']
})
export class SingleUserBoxComponent implements OnInit {
    @Input() user;

    constructor() {
    }

    ngOnInit() {
    }

}
