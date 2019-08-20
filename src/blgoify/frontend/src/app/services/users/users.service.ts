import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
    providedIn: 'root'
})
export class UsersService {
    private usersEndpint = '/api/users';

    constructor(private httpClient: HttpClient) { }

    getUser(uuid: string) {
        return this.httpClient.get(`${this.usersEndpint}/user/${uuid}`);
    }

    /* Temporary */
    getAllUsers() {
        return this.httpClient.get(this.usersEndpint);
    }
}
