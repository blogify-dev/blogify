import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from 'src/app/models/User';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    currentUserToken: Promise<UserToken>;
    currentUser: Promise<User>;

    constructor(private httpClient: HttpClient) { }

    login(user: LoginCredentials) {
        const token = this.httpClient.post<UserToken>('/api/auth/signin', user);
        this.currentUserToken = token.toPromise();
        return token
    }

    register(user: RegisterCredentials) {
        return this.httpClient.post<RegisterCredentials>('/api/auth/signup', user);
    }

    async getUser(uuid: string): Promise<User> {
        const cUser = await this.currentUser;
        if (cUser != undefined /*&& cUser.uuid === uuid*/) {
            return this.currentUser
        } else {
            return this.requestUser(uuid).toPromise()
        }
    }

    private requestUser(uuid: string): Observable<User> {
          const user = this.httpClient.get<User>(`/api/users/${uuid}`);
          this.currentUser = user.toPromise();
        return user
    }

    getUserUUID(token: UserToken): Observable<UserUUID> {
        return this.httpClient.get<UserUUID>(`/api/auth/${token.token}`)
    }

}

interface UserToken {
  token: string
}

interface UserUUID {
    uuid: string
}
