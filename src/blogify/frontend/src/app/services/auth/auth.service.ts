import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from 'src/app/models/User';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly dummyUser = new User('', '');

    private currentUserToken_ = new BehaviorSubject('');
    private currentUserUuid_ = new BehaviorSubject('');
    private currentUser_ = new BehaviorSubject(this.dummyUser);

    constructor(private httpClient: HttpClient) {}

    async login(user: LoginCredentials): Promise<UserToken> {
        const token = this.httpClient.post<UserToken>('/api/auth/signin', user, {responseType: "json"});
        const it = await token.toPromise();

        console.log(`it.token: ${it.token}`);

        this.currentUserToken_.next(it.token);

        const uuid = await this.getUserUUIDFromToken(it.token);
        const fetchedUser = await this.fetchUser(uuid.uuid);

        console.log(fetchedUser);

        this.currentUser_.next(fetchedUser);
        this.currentUserUuid_.next(fetchedUser.uuid);

        console.log(this.userUUID);

        return it
    }

    async register(credentials: RegisterCredentials): Promise<User> {
        return this.httpClient.post<User>('/api/auth/signup', credentials).toPromise();
    }

    private async getUserUUIDFromToken(token: string): Promise<UserUUID> {
        const userUUIDObservable = this.httpClient.get<UserUUID>(`/api/auth/${token}`);
        const uuid = await userUUIDObservable.toPromise();
        this.currentUserUuid_.next(uuid.uuid);
        return uuid;
    }

    async fetchUser(uuid: string): Promise<User> {
        return this.httpClient.get<User>(`/api/users/${uuid}`).toPromise()
    }

    get userToken(): string {
        return this.currentUserToken_.getValue()
    }

    get userUUID(): string {
        return this.currentUserUuid_.getValue()
    }

    get userProfile(): User {
        return this.currentUser_.getValue()
    }

}

interface UserToken {
    token: string
}

interface UserUUID {
    uuid: string
}
