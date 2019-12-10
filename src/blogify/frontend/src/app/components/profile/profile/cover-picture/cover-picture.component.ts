import { Component, Input, OnInit } from '@angular/core';
import { StaticContentService } from '../../../../services/static/static-content.service';
import { StaticFile } from "../../../../models/Static";

@Component({
    selector: 'app-cover-picture',
    templateUrl: './cover-picture.component.html',
    styleUrls: ['./cover-picture.component.scss']
})
export class CoverPictureComponent implements OnInit {

    @Input() cvpFile: StaticFile;

    sourceUrl: string | null = null;

    constructor(private staticContentService: StaticContentService) {}

    ngOnInit() {
        if (this.cvpFile.fileId !== undefined) {
            this.sourceUrl = this.staticContentService.urlFor(this.cvpFile);
        }
    }

}
