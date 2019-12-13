/* tslint:disable:variable-name */
import {Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from 'src/app/models/User';
import { BehaviorSubject, Observable } from 'rxjs';
import { StaticFile } from '../../models/Static';
import { StaticContentService } from '../../services/static/static-content.service';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly dummyUser: User = new User('', '', '', '', new StaticFile('-1'), new StaticFile('-1'));

    private currentUserUuid_ = new BehaviorSubject('');
    private currentUser_ = new BehaviorSubject(this.dummyUser);
    private loginObservable_ = new BehaviorSubject<boolean>(false);

    constructor (
        private httpClient: HttpClient,
        private staticContentService: StaticContentService,
    ) {
        this.attemptRestoreLogin()
    }

    private attemptRestoreLogin() {
        const token = AuthService.attemptFindLocalToken();
        if (token == null) {
            console.info('[blogifyAuth] No stored token');
        } else {
            this.login(token).then (
                () => {
                    console.info('[blogifyAuth] Logged in with stored token')
                }, () => {
                    console.error('[blogifyAuth] Error while attempting stored token, not logging in and clearing token.');
                    localStorage.removeItem('userToken');
                });
        }
    }

    private static attemptFindLocalToken(): string | null {
        return localStorage.getItem('userToken');
    }

    async login(creds: LoginCredentials | string): Promise<UserToken> {

        let token: Observable<UserToken>;
        let it: UserToken;

        if (typeof creds !== 'string') { // user / password
            token = this.httpClient.post<UserToken>('/api/auth/signin', creds, { responseType: 'json' });
            it = await token.toPromise();

            localStorage.setItem('userToken', it.token);
        } else { // token
            it = { token: creds }
        }

        const uuid = await this.getUserUUIDFromToken(it.token);

        // Fix JS bullshit
        const fetchedUserObj: User = await this.fetchUser(uuid);
        const fetchedUser = new User(fetchedUserObj.uuid, fetchedUserObj.username, fetchedUserObj.name, fetchedUserObj.email, fetchedUserObj.profilePicture, fetchedUserObj.coverPicture);

        this.currentUser_.next(fetchedUser);
        this.currentUserUuid_.next(fetchedUser.uuid);
        this.loginObservable_.next(true);

        return it
    }

    logout() {
        localStorage.removeItem("userToken");
        this.loginObservable_.next(false);
    }

    async register(credentials: RegisterCredentials): Promise<User> {
        return this.httpClient.post<User>('/api/auth/signup', credentials).toPromise();
    }

    observeIsLoggedIn(): Observable<boolean> {
        return this.loginObservable_;
    }

    private async getUserUUIDFromToken(token: string): Promise<string> {
        const uuid = await this.httpClient.get<UserUUID>(`/api/auth/${token}`).toPromise();
        this.currentUserUuid_.next(uuid.uuid);
        return uuid.uuid;
    }

    async fetchUser(uuid: string): Promise<User> {
        return this.httpClient.get<User>(`/api/users/${uuid}`).toPromise()
    }

    // noinspection JSMethodCanBeStatic
    get userToken(): string | null {
        return localStorage.getItem('userToken');
    }

    get userUUID(): Promise<string> {
        if (this.currentUserUuid_.getValue())
            return Promise.resolve(this.currentUserUuid_.getValue());
        else {
            const uuid = this.getUserUUIDFromToken(this.userToken);
            uuid.then(it => {
                console.log(it);
                this.currentUserUuid_.next(it);
            });
            return uuid;
        }
    }

    get userProfile(): Promise<User> {
        return this.getUser()
    }

    private async getUser(): Promise<User> {
        if (this.currentUser_.getValue().uuid != '') {
            return this.currentUser_.getValue()
        } else {
            return this.fetchUser(await this.userUUID)
        }
    }

    getByUsername(username: string): Promise<User> {
        return this.httpClient.get<User>(`/api/users/byUsername/${username}`).toPromise()
    }

    async uploadFile(file: File, uploadableName: string) {
        return this.staticContentService.uploadFile(file, this.userToken, `/api/users/upload/${await this.userUUID}/?target=${uploadableName}`)
    }

    search(query: string, fields: string[]): Promise<User[]> {
        const url = `/api/articles/search/?q=${query}&fields=${fields.join(',')}`;
        return this.httpClient.get<User[]>(url).toPromise()
    }

    getAllUsers(): Promise<User[]> {
        return this.httpClient.get<User[]>('/api/users').toPromise();
    }
}

interface UserToken {
    token: string
}

interface UserUUID {
    uuid: string
}
