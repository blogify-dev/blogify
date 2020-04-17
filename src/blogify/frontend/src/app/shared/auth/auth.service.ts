/* tslint:disable:variable-name */
import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from 'src/app/models/User';
import { BehaviorSubject, Observable } from 'rxjs';
import { StaticFile } from '../../models/Static';
import { StaticContentService } from '../../services/static/static-content.service';
import { SearchView } from '../../models/SearchView';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor (
        private httpClient: HttpClient,
        private staticContentService: StaticContentService,
    ) {
        this.attemptRestoreLogin();
    }

    // noinspection JSMethodCanBeStatic
    get userToken(): string | null {
        const item = localStorage.getItem('userToken');

        if (!item || item === '')
            return null;
        return item;
    }

    get userUUID(): Promise<string> {
        if (this.currentUserUuid_.getValue()) {
            return Promise.resolve(this.currentUserUuid_.getValue());
        } else {
            const uuid = this.getUserUUIDFromToken(this.userToken);
            uuid.then(it => {
                console.log(it);
                this.currentUserUuid_.next(it);
            });
            return uuid;
        }
    }

    get userProfile(): Promise<User> {
        return this.loginObservable_.value ? this.getUser() : null;
    }

    private readonly dummyUser: User = new User('', '', '', '', [], false, new StaticFile('-1'), new StaticFile('-1'));

    private currentUserUuid_ = new BehaviorSubject('');
    private currentUser_ = new BehaviorSubject(this.dummyUser);
    private loginObservable_ = new BehaviorSubject<boolean>(false);

    private attemptRestoreLogin() {
        const token = this.userToken;
        if (token == null) {
            console.info('[blogifyAuth] No stored token');
        } else {
            this.login(token).then (
                () => {
                    console.info('[blogifyAuth] Logged in with stored token');
                }, () => {
                    console.error('[blogifyAuth] Error while attempting stored token, not logging in and clearing token.');
                    localStorage.removeItem('userToken');
                });
        }
    }

    async login(creds: LoginCredentials | string): Promise<User> {
        const token = (typeof creds === 'string') ? creds :
            (await this.httpClient.post<UserToken>('/api/auth/signin', creds, {responseType: 'json'})
                .toPromise()).token

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            }),
        };

        const user = await this.httpClient.get<User>('/api/users/me/', httpOptions).toPromise();

        // We have reached a point where `token` is valid so we populate the cache with it
        localStorage.setItem('userToken', token);

        this.currentUser_.next(user);
        this.currentUserUuid_.next(user.uuid);
        this.loginObservable_.next(true);

        return user;
    }

    logout() {
        localStorage.removeItem('userToken');
        this.currentUser_.next(this.dummyUser);
        this.currentUserUuid_.next('');
        this.loginObservable_.next(false);
    }

    async register(credentials: RegisterCredentials): Promise<User> {
        return this.httpClient.post<User>('/api/auth/signup', credentials).toPromise();
    }

    observeIsLoggedIn(): Observable<boolean> {
        return this.loginObservable_.asObservable();
    }

    private async getUserUUIDFromToken(token: string): Promise<string> {
        const uuid = await this.httpClient.get<UserUUID>(`/api/auth/${token}`).toPromise();
        this.currentUserUuid_.next(uuid.uuid);
        return uuid.uuid;
    }

    async fetchUser(uuid: string): Promise<User> {
        return this.httpClient.get<User>(`/api/users/${uuid}`).toPromise();
    }

    private async getUser(): Promise<User> {
        if (this.currentUser_.getValue().uuid !== '') {
            return this.currentUser_.getValue();
        } else {
            return this.fetchUser(await this.userUUID);
        }
    }

    async getByUsername(username: string): Promise<User> {
        return this.httpClient.get<User>(`/api/users/byUsername/${username}`).toPromise();
    }

    async uploadFile(file: File, uploadableName: string) {
        return this.staticContentService.uploadFile(
            file,
            this.userToken,
            `/api/users/upload/${await this.userUUID}/?target=${uploadableName}`
        );
    }

    search(query: string, fields: string[]) {
        const url = `/api/users/search/?q=${query}&fields=${fields.join(',')}`;
        return this.httpClient.get<SearchView<User>>(url).toPromise();
    }

    async getAllUsers(): Promise<User[]> {
        return this.httpClient.get<User[]>('/api/users').toPromise();
    }

    async fillUsersFromUUIDs(uuids: string[]): Promise<User[]> {
        return Promise.all(uuids.map(it => this.fetchUser(it)));
    }

}

interface UserToken {
    token: string;
}

interface UserUUID {
    uuid: string;
}
