import { Injectable } from '@angular/core';
import { StaticFile } from '../../models/Static';
import {HttpClient, HttpHeaders} from "@angular/common/http";

@Injectable({
    providedIn: 'root'
})
export class StaticContentService {

    constructor(private httpClient: HttpClient) {}

    urlFor(file: StaticFile): string {
        return `/api/get/${file.fileId}`;
    }

    uploadFile(file: File, userToken: string, url: string) {
        console.log(url);
        console.log(file.name);

        const httpOptions = {
            headers: new HttpHeaders({
                Authorization: `Bearer ${userToken}`
            })
        };

        const input = new FormData();
        input.append('lUcY', file);

        return this.httpClient.post(url, input, httpOptions).toPromise()

    }

}
