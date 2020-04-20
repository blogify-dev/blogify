import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { User } from '../../../models/User';
import {StateService} from "../state/state.service";

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private httpClient: HttpClient, private stateService: StateService) {}

    async fetchOrGetUser(uuid: string): Promise<User> {
        const cached = this.stateService.getUser(uuid)
        if (cached) {
            console.log(`[user ${uuid}]: returning from cache`)
            return cached
        }
        const fetched = await this.httpClient.get<User>(`/api/users/${uuid}`).toPromise();
        this.stateService.cacheUser(fetched)
        console.log(`[user ${uuid}]: fetching`)
        return fetched
    }

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
