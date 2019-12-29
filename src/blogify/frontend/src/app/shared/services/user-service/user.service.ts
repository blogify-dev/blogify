import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { User } from '../../../models/User';

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private httpClient: HttpClient) {}

    async toggleFollowUser(user: User, userToken: string): Promise<HttpResponse<Object>> {

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            }),
            observe: 'response',
        };

        // TypeScript bug with method overloads.
        // @ts-ignore
        return this.httpClient.post<HttpResponse<Object>>(`/api/users/${user.uuid}/follow`, null, httpOptions).toPromise()
    }

}
