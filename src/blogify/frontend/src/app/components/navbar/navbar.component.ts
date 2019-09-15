import { Component, OnInit } from '@angular/core';
import { AuthService } from "../../services/auth/auth.service";
import { Router } from "@angular/router";

@Component({
    selector: 'app-navbar',
    templateUrl: './navbar.component.html',
    styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {

    constructor(private authService: AuthService, private router: Router) {
    }

    ngOnInit() {
        console.log(this.authService.userToken)
    }

    async navigateToProfile() {
        const url = `/profile/${await this.authService.getUserUUID(this.authService.userToken)}`
        await this.router.navigateByUrl(url)
    }

    async navigateToNewArticle() {
        if (this.authService.userToken == '') {
            await this.router.navigateByUrl('/login')
        } else {
            await this.router.navigateByUrl('/new-article')

        }
    }
}
