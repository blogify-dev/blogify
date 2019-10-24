import { Component, Input, OnInit } from '@angular/core';
import { StaticContentService } from '../../services/static/static-content.service';
import {StaticFile} from "../../models/Static";

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
        this.sourceUrl = this.staticContentService.urlFor(this.pfpFile);
    }

    handleError() {
        this.erroredOut = true;
    }

}
