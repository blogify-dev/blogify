import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { UsernamePasswordCredentials, User } from 'src/app/models/User';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor(private httpClient: HttpClient) { }

    login(user: UsernamePasswordCredentials): Observable<string> {
        return this.httpClient.post<string>('/api/auth/signin', user)
    }

    register(user: User) {
        return this.httpClient.post<User>('/api/auth/signup', user)
    }

    getUser(uuid: string, userToken: string): Observable<User> {
        const httpOptions = {
            headers: new HttpHeaders({
              'Content-Type':  'application/json',
              'Authorization': `Bearer ${userToken}`
            })
          };
        return this.httpClient.get<User>(`/api/user/${uuid}`, httpOptions)
    }

}
