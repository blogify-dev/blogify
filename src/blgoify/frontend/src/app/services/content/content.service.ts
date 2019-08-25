import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ContentService {
  private contentEndpoint = '/articles/content';

  constructor(private httpClient: HttpClient) {
  }

  getContent(uuid: string) {
    return this.httpClient.get(`${this.contentEndpoint}/${uuid}`);
  }

}
