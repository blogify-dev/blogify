import { Component, OnInit } from '@angular/core';
import { Toast } from '../../services/toaster/models/Toast';
import { timer } from 'rxjs';
import {DomSanitizer} from '@angular/platform-browser';

@Component({
    selector: 'app-toaster',
    templateUrl: './toaster.component.html',
    styleUrls: ['./toaster.component.scss']
})
export class ToasterComponent implements OnInit {

    private toastQueue: Toast[] = [];
    private currentToast: Toast | null;

    constructor(private domSanitizer: DomSanitizer) {}

    ngOnInit() {
        // Check for new toast
        timer(0, 250).subscribe(_ => {
            if (this.currentToast == null) {
                this.popQueue();
            }
        });
        // Clear stale toast
        timer(0, 5000).subscribe(_ => {
            this.currentToast = null;
            if (this.toastQueue.length != 0) {
                this.popQueue();
            }
        })
    }

    private popQueue() {
        const toastToPop = this.toastQueue.pop();
        this.currentToast = toastToPop ? toastToPop : null;
    }

    bake(...toast: Toast[]) {
        this.toastQueue.push(...toast);
        toast.forEach(it => {
            it.backgroundColor = this.domSanitizer.bypassSecurityTrustStyle(<string> it.backgroundColor);
            it.foregroundColor = this.domSanitizer.bypassSecurityTrustStyle(<string> it.foregroundColor);
        });
    }

}
