import { Component, Input, OnInit } from '@angular/core';
import { User } from '@blogify/models/User';
import { faCopy } from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from 'ngx-clipboard';
import { StaticContentService } from '@blogify/core/services/static/static-content.service';

@Component({
    selector: 'app-single-user-box',
    templateUrl: './single-user-box.component.html',
    styleUrls: ['./single-user-box.component.scss']
})
export class SingleUserBoxComponent implements OnInit {

    @Input() user: User;

    faCopy = faCopy;

    cvpPath: string;

    constructor (
        private staticContentService: StaticContentService,
        private clipboardService: ClipboardService,
    ) {}

    ngOnInit() {
        this.cvpPath = this.staticContentService.urlFor(this.user.coverPicture);
    }

    copyLinkToClipboard() {
        this.clipboardService.copyFromContent(window.location.href);
    }

}
