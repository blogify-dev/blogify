import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { LoginCredentials, RegisterCredentials, User } from '@blogify/models/User';
import { BehaviorSubject, Observable } from 'rxjs';
import { StaticContentService } from '@blogify/core/services/static/static-content.service';

const USER_TOKEN_KEY = 'userToken';
const KEEP_LOGGED_IN_KEY = 'keepLoggedIn';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    constructor (
        private httpClient: HttpClient,
        private staticContentService: StaticContentService,
    ) {}

    private currentUserSubject = new BehaviorSubject<CurrentUser>(null);
    private isLoggedInSubject = new BehaviorSubject<boolean>(false);


    setupAndPopulateCache() {
        const cachedToken = localStorage.getItem(USER_TOKEN_KEY);
        if (cachedToken !== null) {
            this.httpClient.get<User>('/api/users/me', {
                headers: new HttpHeaders({ Authorization: `Bearer ${cachedToken}` })
            }).toPromise().then(currentUser => {
                this.currentUserSubject.next({ ...currentUser, token: cachedToken });
                this.isLoggedInSubject.next(true);
            })
                .catch(error => {
                    console.error(error);
                    localStorage.removeItem(USER_TOKEN_KEY);
                });
        }
    }

    get currentUser(): CurrentUser | null {
        return this.currentUserSubject.value;
    }

    async login(creds: LoginCredentials | string, keepLoggedIn = false): Promise<User> {
        const signin = async (): Promise<string> => {
            localStorage.setItem(KEEP_LOGGED_IN_KEY, `${keepLoggedIn}`);
            return (await this.httpClient.post<{ token: string }>('/api/auth/signin', creds, { responseType: 'json' })
                .toPromise()).token;
        };

        const token = (typeof creds === 'string') ? creds : (await signin());

        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                Authorization: `Bearer ${token}`
            }),
        };

        const user = await this.httpClient.get<User>('/api/users/me/', httpOptions).toPromise();

        const shouldKeepLoggedIn = localStorage.getItem(KEEP_LOGGED_IN_KEY) === 'true';
        if (shouldKeepLoggedIn) {
            // We have reached a point where `token` is valid so we populate the cache with it
            localStorage.setItem('userToken', token);
        }

        this.currentUserSubject.next({ ...user, token: token });
        this.isLoggedInSubject.next(true);

        return user;
    }

    logout() {
        localStorage.removeItem('userToken');
        this.isLoggedInSubject.next(false);
    }

    async register(credentials: RegisterCredentials): Promise<User> {
        const resp = await this.httpClient.post<SignupPayload>('/api/auth/signup', credentials).toPromise();

        return await this.login(resp.token);
    }

    observeIsLoggedIn(): Observable<boolean> {
        return this.isLoggedInSubject.asObservable();
    }

    async uploadFile(file: File, uploadableName: string) {
        return this.staticContentService.uploadFile(
            file,
            this.currentUser.token,
            `/api/users/upload/${this.currentUser.uuid}/?target=${uploadableName}`
        );
    }

}

interface SignupPayload {
    user: User;
    token: string;
}

type CurrentUser = User & { token: string }
