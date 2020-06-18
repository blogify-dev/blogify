import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Filter } from '@blogify/shared/components/content-feed/filtering/Filter';
import { faTimes } from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'b-active-filter',
    templateUrl: './active-filter.component.html',
    styleUrls: ['./active-filter.component.scss']
})
export class ActiveFilterComponent implements OnInit {
    
    faTimes = faTimes;
    
    @Input() filter: Filter;

    @Output() removed = new EventEmitter();
    @Output() updated = new EventEmitter();

    constructor() {}

    ngOnInit() {}

}
