import { Injectable } from '@angular/core';
import { StaticFile } from '../../models/Static';

@Injectable({
    providedIn: 'root'
})
export class StaticContentService {

    constructor() {}

    profilePictureUrl = (userUUID: string, fileId: string): string => `/api/get/${fileId}`;

}
