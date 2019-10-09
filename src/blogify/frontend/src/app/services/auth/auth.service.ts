import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from 'src/app/models/User';
import { BehaviorSubject, Observable } from 'rxjs';
import {Article} from '../../models/Article';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly dummyUser: User = new User('', '', '', '');

    private currentUserToken_ = new BehaviorSubject('');
    private currentUserUuid_ = new BehaviorSubject('');
    private currentUser_ = new BehaviorSubject(this.dummyUser);

    constructor(private httpClient: HttpClient) {}

    async login(user: LoginCredentials): Promise<UserToken> {
        const token = this.httpClient.post<UserToken>('/api/auth/signin', user, {responseType: "json"});
        const it = await token.toPromise();

        this.currentUserToken_.next(it.token);

        const uuid = await this.getUserUUIDFromToken(it.token);

        // Fix JS bullshit
        const fetchedUserObj: User = await this.fetchUser(uuid.uuid);
        const fetchedUser = new User(fetchedUserObj.uuid, fetchedUserObj.username, fetchedUserObj.name, fetchedUserObj.email);

        console.log("TYPE CHECK: " + (fetchedUser instanceof User));

        this.currentUser_.next(fetchedUser);
        this.currentUserUuid_.next(fetchedUser.uuid);

        return it
    }

    async register(credentials: RegisterCredentials): Promise<User> {
        return this.httpClient.post<User>('/api/auth/signup', credentials).toPromise();
    }

    isLoggedIn(): boolean {
        return this.userToken !== '';
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
