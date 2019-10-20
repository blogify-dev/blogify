import { Component, Input, OnInit } from '@angular/core';
import { StaticFile } from '../../models/Static';
import { StaticContentService } from '../../services/static/static-content.service';

@Component({
    selector: 'app-profile-picture',
    templateUrl: './profile-picture.component.html',
    styleUrls: ['./profile-picture.component.scss']
})
export class ProfilePictureComponent implements OnInit {

    @Input() pfpFile: StaticFile;
    @Input() emSize: number;

    sourceUrl: string;
    erroredOut = false;

    constructor(private staticContentService: StaticContentService) {}

    ngOnInit() {
        if (this.pfpFile === undefined || this.pfpFile === null || this.pfpFile.id === -1) {
            this.handleError();
        } else {
            this.sourceUrl = this.staticContentService.urlFor(this.pfpFile);
        }
    }

    handleError() {
        this.erroredOut = true;
        console.log('FUCK');
    }

}
