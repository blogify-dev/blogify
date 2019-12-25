import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { StaticContentService } from '../../../../services/static/static-content.service';
import { StaticFile } from "../../../../models/Static";

@Component({
    selector: 'app-cover-picture',
    templateUrl: './cover-picture.component.html',
    styleUrls: ['./cover-picture.component.scss']
})
export class CoverPictureComponent implements OnInit, OnChanges {

    @Input() cvpFile: StaticFile;

    sourceUrl: string | null = null;

    constructor(private staticContentService: StaticContentService) {}

    ngOnInit() {}

    ngOnChanges(changes: SimpleChanges): void {
        this.sourceUrl = this.cvpFile.fileId ? this.staticContentService.urlFor(this.cvpFile) : null;
    }

}
