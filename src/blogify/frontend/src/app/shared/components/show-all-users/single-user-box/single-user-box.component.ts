import { Component, Input, OnInit } from '@angular/core';
import { User } from '../../../../models/User';
import { faCopy } from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from 'ngx-clipboard';

@Component({
    selector: 'app-single-user-box',
    templateUrl: './single-user-box.component.html',
    styleUrls: ['./single-user-box.component.scss']
})
export class SingleUserBoxComponent implements OnInit {
    @Input() user: User;

    faCopy = faCopy;

    constructor(
        private clipboardService: ClipboardService,
    ) { }

    ngOnInit() {
    }

    copyLinkToClipboard() {
        this.clipboardService.copyFromContent(window.location.href);
    }

}
