import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {LoginCredentials, RegisterCredentials, User} from 'src/app/models/User';
import {BehaviorSubject, Observable} from 'rxjs';
import {StaticFile} from '../../models/Static';
import {StaticContentService} from "../static/static-content.service";

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly dummyUser: User = new User('', '', '', '', new StaticFile('-1'));

    private currentUserUuid_ = new BehaviorSubject('');
    private currentUser_ = new BehaviorSubject(this.dummyUser);

    constructor(private httpClient: HttpClient, private staticContentService: StaticContentService) {
        if (localStorage.getItem('userToken') !== null) {
            this.login(localStorage.getItem('userToken'))
        }
    }

    async login(creds: LoginCredentials | string): Promise<UserToken> {

        let token: Observable<UserToken>;
        let it: UserToken;

        if (typeof creds !== 'string') {
            token = this.httpClient.post<UserToken>('/api/auth/signin', creds, { responseType: 'json' });
            it = await token.toPromise();

            localStorage.setItem('userToken', it.token);
        } else {
            it = { token: creds }
        }

        const uuid = await this.getUserUUIDFromToken(it.token);

        // Fix JS bullshit
        const fetchedUserObj: User = await this.fetchUser(uuid);
        const fetchedUser = new User(fetchedUserObj.uuid, fetchedUserObj.username, fetchedUserObj.name, fetchedUserObj.email, fetchedUserObj.profilePicture);

        this.currentUser_.next(fetchedUser);
        this.currentUserUuid_.next(fetchedUser.uuid);

        return it
    }

    async register(credentials: RegisterCredentials): Promise<User> {
        return this.httpClient.post<User>('/api/auth/signup', credentials).toPromise();
    }

    isLoggedIn(): boolean {
        return this.userToken !== null;
    }

    private async getUserUUIDFromToken(token: string): Promise<string> {
        const uuid = await this.httpClient.get<UserUUID>(`/api/auth/${token}`).toPromise();
        this.currentUserUuid_.next(uuid.uuid);
        return uuid.uuid;
    }

    async fetchUser(uuid: string): Promise<User> {
        return this.httpClient.get<User>(`/api/users/${uuid}`).toPromise()
    }

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

    logout() {
        localStorage.removeItem("userToken")
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

    addProfilePicture(file: File, userUUID: string, userToken: string = this.userToken) {
        return this.staticContentService.uploadFile(file, userToken, `/api/users/profilePicture/${userUUID}/?target=profilePicture`)
    }

}

interface UserToken {
    token: string
}

interface UserUUID {
    uuid: string
}
