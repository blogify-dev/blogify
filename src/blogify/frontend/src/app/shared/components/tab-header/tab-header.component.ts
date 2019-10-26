import { Component, Input, OnInit } from '@angular/core';
import {ActivatedRoute, Params, Router} from '@angular/router';

@Component({
    selector: 'app-tab-header',
    templateUrl: './tab-header.component.html',
    styleUrls: ['./tab-header.component.scss']
})
export class TabHeaderComponent implements OnInit {

    @Input() tabs: TabList;

    activatedTab = 0;

    constructor() {}

    ngOnInit() {}

}

export class Tab {
    constructor (
        public tabName: string,
        public tabRouterLink: string
    ) {}
}

export type TabList = Tab[]
