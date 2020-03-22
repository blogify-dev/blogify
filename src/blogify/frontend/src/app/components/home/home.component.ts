import { Component, OnInit } from '@angular/core';
import { ListingQuery } from '../../models/ListingQuery';
import { Article } from '../../models/Article';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

    title = 'blogify';

    listing = new ListingQuery<Article>(4, 0);

    constructor() {}

    ngOnInit() {}

}
