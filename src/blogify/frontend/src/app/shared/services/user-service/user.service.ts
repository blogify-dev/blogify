import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { User } from '../../../models/User';
import { StateService } from '../state/state.service';
import { SearchView } from "../../../models/SearchView";
import { idOf, Shadow } from "../../../models/Shadow";

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

    async updateUser(user: Shadow<User>, data: { [k in keyof User]?: User[k] }, userToken: string): Promise<object> {
        const url = `/api/users/${idOf(user)}`;

        const options = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            })
        };

        return this.httpClient.patch(url, data, options).toPromise();
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

    async getByUsername(username: string): Promise<User> {
        return this.httpClient.get<User>(`/api/users/byUsername/${username}`).toPromise();
    }

    async toggleUserAdmin(user: Shadow<User>, userToken: string): Promise<object> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${userToken}`
            }),
            observe: 'response',
        };

        // TypeScript bug with method overloads.
        // @ts-ignore
        return this.httpClient.post<HttpResponse<object>>(`/api/users/${idOf(user)}/toggleAdmin`, null, httpOptions).toPromise();
    }

    async fillUsersFromUUIDs(uuids: string[]): Promise<User[]> {
        return Promise.all(uuids.map(it => this.getUser(it)));
    }

    async getAllUsers(): Promise<User[]> {
        return this.httpClient.get<User[]>('/api/users').toPromise();
    }

    search(query: string, fields: string[]) {
        const url = `/api/users/search/?q=${query}&fields=${fields.join(',')}`;
        return this.httpClient.get<SearchView<User>>(url).toPromise();
    }

}
