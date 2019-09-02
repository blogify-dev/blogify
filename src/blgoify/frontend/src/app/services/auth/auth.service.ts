import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { UsernamePasswordCredentials, User } from 'src/app/models/User';
import { Observable, of } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    currentUserToken: Promise<string>;
    currentUser: Promise<User>;

    constructor(private httpClient: HttpClient) { }

    login(user: UsernamePasswordCredentials) {
        const token = this.httpClient.post<string>('/api/auth/signin', user);
        this.currentUserToken = token.toPromise()
        return token
    }

    register(user: User) {
        return this.httpClient.post<User>('/api/auth/signup', user);
    }

    async getUser(uuid: string, userToken: string): Promise<User> {
        const cUser = await this.currentUser
        if (cUser != undefined /*&& cUser.uuid === uuid*/) {
            return this.currentUser
        } else {
            return this.requestUser(uuid, userToken).toPromise()
        }
    }

    private requestUser(uuid: string, userToken: string): Observable<User> {
        const httpOptions = {
            headers: new HttpHeaders({
              'Content-Type':  'application/json',
              Authorization: `Bearer ${userToken}`
            })
          };
          const user = this.httpClient.get<User>(`/api/user/${uuid}`, httpOptions);
          this.currentUser = user.toPromise()
        return user
    }

}
