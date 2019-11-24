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
    currentToast: Toast | null;
    clearingTopState = 2;

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
            if (this.toastQueue.length >= 1) { // More than one toast are left
                this.popQueue();
            } else {
                if (this.clearingTopState == 0) { // Next cycle after transition -> remove it
                    this.popQueue();
                    this.currentToast = null;
                    this.clearingTopState = 3;
                } else if (this.clearingTopState == 1 || this.clearingTopState == 2) {
                    this.clearingTopState--;
                } else {
                    this.clearingTopState--; // Triggers transition
                }
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
