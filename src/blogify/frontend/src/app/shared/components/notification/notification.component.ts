import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Notification } from '@blogify/models/Notification';
import { ToastRef } from 'ngx-toastr';
import { Router } from '@angular/router';
import { faTrashAlt } from '@fortawesome/free-regular-svg-icons';

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

    faTrashAlt = faTrashAlt;

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
