import { Injectable } from '@angular/core';
import {webSocket} from "rxjs/webSocket";
import {AuthService} from "../../auth/auth.service";

@Injectable({
    providedIn: 'root'
})
export class PushService {

    private ws = webSocket<string>({
        url: 'ws://' + document.location.hostname + '/push/connect',
        deserializer: event => event.data as string,
        serializer:   data  => data
    });

    private authenticated = false;

    constructor(private authService: AuthService) {
        this.authService.observeIsLoggedIn().subscribe(loggedIn => {
            if (loggedIn) {
                this.ws.next(this.authService.userToken);
                this.ws.subscribe((msg) => {
                    if (!this.authenticated) {
                        if (msg.match(/AUTH OK/)) {
                            this.authenticated = true;
                        }
                    } else {
                        console.log(msg);
                    }
                });
            }
        })
    }

}
