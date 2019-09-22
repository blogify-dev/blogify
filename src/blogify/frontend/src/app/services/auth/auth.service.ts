import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from 'src/app/models/User';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private currentUserToken_ = new BehaviorSubject('');
    private readonly dummyUser = new User('', '')
    private currentUser_ = new BehaviorSubject(this.dummyUser);
    private currentUserUuid_ = new BehaviorSubject('');

    constructor(private httpClient: HttpClient) {}

    async login(user: LoginCredentials): Promise<UserToken> {
        const token = this.httpClient.post<UserToken>('/api/auth/signin', user);
        const it = await token.toPromise();

        console.log(`it.token: ${it.token}`);

        this.currentUserToken_.next(it.token);

        const uuid = await this.getUserUUIDFromToken(it.token);
        const fetchedUser = await this.getUser(uuid.uuid);

        console.log(fetchedUser);

        this.currentUser_.next(fetchedUser);
        this.currentUserUuid_.next(fetchedUser.uuid);

        console.log(this.userUUID);

        return it
    }

    register(user: RegisterCredentials): Observable<RegisterCredentials> {
        return this.httpClient.post<RegisterCredentials>('/api/auth/signup', user);
    }

    private async requestUser(uuid: string): Promise<User> {
        const userObservable = this.httpClient.get<User>(`/api/users/${uuid}`);
        const user = await userObservable.toPromise();
        this.currentUser_.next(user);
        return user
    }

    async getUserUUIDFromToken(token: string): Promise<UserUUID> {
        const userUUIDObservable = this.httpClient.get<UserUUID>(`/api/auth/${token}`);
        const uuid = await userUUIDObservable.toPromise();
        this.currentUserUuid_.next(uuid.uuid);
        return uuid;
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

    async getUser(uuid: string): Promise<User> {
        const cUserVal = this.currentUser_.getValue();
        if (cUserVal == this.dummyUser || cUserVal.username == '') {
            await this.requestUser(uuid)
        }
        return this.currentUser_.getValue()
    }
}


interface UserToken {
    token: string
}

interface UserUUID {
    uuid: string
}
