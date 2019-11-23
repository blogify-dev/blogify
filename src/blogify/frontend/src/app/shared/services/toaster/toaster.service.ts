import { Injectable } from '@angular/core';
import { Toast } from './models/Toast';
import { ToasterComponent } from '../../components/toaster/toaster.component';

@Injectable({
    providedIn: 'root'
})
export class ToasterService {

    private pluggedToaster: ToasterComponent | null;

    constructor() {}

    plugInto(toaster: ToasterComponent) {
        this.pluggedToaster = toaster
    }

    feed(...toast: Toast[]) {
        if (this.pluggedToaster != null) {
            this.pluggedToaster.bake(...toast.reverse())
        } else console.error("[blogifyToaster] No destination for toast !")
    }

}
