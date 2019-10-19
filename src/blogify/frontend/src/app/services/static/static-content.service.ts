import { Injectable } from '@angular/core';
import { StaticFile } from '../../models/Static';

@Injectable({
    providedIn: 'root'
})
export class StaticContentService {

    constructor() {}

    urlFor(file: StaticFile): string {
      return `http://192.168.122.56:8080/api/get/${file.collection}/${file.id}`
    }

}
