import { Component, OnInit } from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Component({
    selector: 'app-overview',
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.scss']
})
export class OverviewComponent implements OnInit {

    constructor(private http: HttpClient) {}

    ngOnInit() {}

    callReindex() {
        this.http.post('/api/admin/search/reindex', null).toPromise().then(r => alert(r));
    }

}
