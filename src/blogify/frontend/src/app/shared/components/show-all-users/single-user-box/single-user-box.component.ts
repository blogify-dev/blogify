import { Component, OnInit } from '@angular/core';
import { User } from '@blogify/models/User';
import { faCopy } from '@fortawesome/free-regular-svg-icons';
import { ClipboardService } from 'ngx-clipboard';
import { StaticContentService } from '@blogify/core/services/static/static-content.service';
import { EntityRenderComponent } from '@blogify/models/entities/EntityRenderComponent';

@Component({
    selector: 'app-single-user-box',
    templateUrl: './single-user-box.component.html',
    styleUrls: ['./single-user-box.component.scss']
})
export class SingleUserBoxComponent extends EntityRenderComponent<User> implements OnInit {

    faCopy = faCopy;

    cvpPath: string;

    constructor (
        private staticContentService: StaticContentService,
        private clipboardService: ClipboardService,
    ) { super(); }

    ngOnInit() {
        this.cvpPath = this.staticContentService.urlFor(this.entity.coverPicture);
    }

    copyLinkToClipboard() {
        this.clipboardService.copyFromContent(window.location.href);
    }

}
