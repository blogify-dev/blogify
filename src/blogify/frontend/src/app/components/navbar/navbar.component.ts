import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth/auth.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

    constructor(public authService: AuthService, private router: Router) {
    }

    ngOnInit() {
        console.log(this.authService.userToken)
    }

    async navigateToProfile() {
        const url = `/profile/${await this.authService.getUserUUIDFromToken(this.authService.userToken)}`
        await this.router.navigateByUrl(url)
    }
}
