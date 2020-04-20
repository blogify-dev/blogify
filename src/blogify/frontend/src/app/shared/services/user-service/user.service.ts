import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { User } from '../../../models/User';
import { StateService } from '../state/state.service';

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private httpClient: HttpClient, private state: StateService) {}

    async getUser(userUuid: string): Promise<User> {
        const cached = this.state.getUser(userUuid);

        if (cached) {
            console.log(`[user ${userUuid}]: returning from cache`);

            return cached;
        } else {
            const fetched = await this.httpClient.get<User>(`/api/users/${userUuid}`).toPromise();
            this.state.cacheUser(fetched);

            console.log(`[user ${userUuid}]: fetched`);

            return fetched;
        }
    }

    async toggleFollowUser(user: User, userToken: string): Promise<HttpResponse<object>> {

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            }),
            observe: 'response',
        };

        // TypeScript bug with method overloads.
        // @ts-ignore
        return this.httpClient.post<HttpResponse<object>>(`/api/users/${user.uuid}/follow`, null, httpOptions).toPromise();
    }

}
