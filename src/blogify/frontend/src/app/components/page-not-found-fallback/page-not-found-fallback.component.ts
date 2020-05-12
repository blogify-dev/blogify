import { Component, Inject, OnInit } from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Component({
    selector: 'b-page-not-found-fallback',
    templateUrl: './page-not-found-fallback.component.html',
    styleUrls: ['./page-not-found-fallback.component.scss'],
})
export class PageNotFoundFallbackComponent implements OnInit {

    constructor(@Inject(DOCUMENT) private document: Document) {
        this.document.location.href = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';
    }

    ngOnInit() {
    }

}
