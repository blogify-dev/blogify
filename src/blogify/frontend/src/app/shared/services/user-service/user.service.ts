import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { User } from "../../../models/User";

@Injectable({
    providedIn: 'root'
})
export class UserService {

    constructor(private httpClient: HttpClient) {}

    follows(uuid: string) {
        return this.httpClient.get<User[]>(`/api/users/${uuid}/follows`).toPromise()
    }
}
