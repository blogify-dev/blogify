import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Notification } from '../../../models/Notification';
import { ToastRef } from 'ngx-toastr';
import { faChevronDown } from '@fortawesome/free-solid-svg-icons';
import { Router } from '@angular/router';

@Component({
    selector: 'b-notification',
    templateUrl: './notification.component.html',
    styleUrls: ['./notification.component.scss']
})
export class NotificationComponent implements OnInit {

    toastRef: ToastRef<any>;

    @Input() notification: Notification;

    @Output() followed = new EventEmitter<any>();
    @Output() dismissed = new EventEmitter<any>();

    faChevronDown = faChevronDown;

    // @ts-ignore
    get iconIsPfp()  { return this.notification.icon.contentType !== undefined;}
    get iconIsFa()   { return !this.iconIsPfp && this.notification !== null; }
    get iconIsNone() { return !this.iconIsPfp && !this.iconIsFa; }

    constructor(private router: Router) {}

    ngOnInit() {}

    navigate() {
        this.router.navigateByUrl(this.notification.routerLink)
            .then(_ => this.followed.emit());
    }

    dismissSelf() {
        if (this.toastRef) this.toastRef.close();
        this.dismissed.emit();
    }

}
