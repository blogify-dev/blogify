import { Component, Input, OnInit } from '@angular/core';
import { StaticContentService } from '../../services/static/static-content.service';

@Component({
    selector: 'app-profile-picture',
    templateUrl: './profile-picture.component.html',
    styleUrls: ['./profile-picture.component.scss']
})
export class ProfilePictureComponent implements OnInit {

    @Input() pfpFileId: string;
    @Input() userUUID: string;
    @Input() emSize: number;

    sourceUrl: string;
    erroredOut = false;

    constructor(private staticContentService: StaticContentService) {}

    ngOnInit() {
        console.log(this.pfpFileId)
        this.sourceUrl = this.staticContentService.profilePictureUrl(this.userUUID, this.pfpFileId);
        console.log(this.sourceUrl)
    }

    handleError() {
        this.erroredOut = true;
        console.log('FUCK');
    }

}
