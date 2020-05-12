import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-tab-header',
    templateUrl: './tab-header.component.html',
    styleUrls: ['./tab-header.component.scss']
})
export class TabHeaderComponent implements OnInit {

    @Input() tabs: TabList;
    @Input() activatedTab = 0;

    currentUrl;

    constructor (
        private router: Router
    ) {}

    ngOnInit() {
        this.currentUrl = this.router.url;
        this.adjustSelectedtab(this.currentUrl);

        this.router.events.subscribe(() => { // weird hack for setting correct tab on profile change, but works. sort of. for now.
            const newUrl = this.router.url;
            if (newUrl !== this.currentUrl) {
                this.currentUrl = newUrl;
                this.adjustSelectedtab(newUrl);
            }
        });
    }

    private adjustSelectedtab(url: string) {
        let newSegment = url.substring(url.lastIndexOf('/') + 1);
        this.activatedTab = this.tabs.findIndex(tab => tab.tabRouterLink === newSegment);
    }

}

export class Tab {
    constructor (
        public tabName: string,
        public tabRouterLink: string
    ) {}
}

export type TabList = Tab[]
