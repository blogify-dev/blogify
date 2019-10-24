import { Injectable } from '@angular/core';
import { StaticFile } from '../../models/Static';

@Injectable({
    providedIn: 'root'
})
export class StaticContentService {

    constructor() {}

    urlFor(file: StaticFile): string {
        return `/api/get/${file.fileId}`;
    }

}
