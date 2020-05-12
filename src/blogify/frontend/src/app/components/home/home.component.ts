import { Component, OnInit } from '@angular/core';
import { ListingQuery } from '@blogify/models/ListingQuery';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

    title = 'blogify';

    listing = new ListingQuery(15, 0);

    constructor() {}

    ngOnInit() {}

}
