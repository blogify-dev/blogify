import { SafeStyle } from '@angular/platform-browser';
import { faInfoCircle, IconDefinition } from '@fortawesome/free-solid-svg-icons';

export enum ToastStyle {
    NEUTRAL  = 'var(--toast-neutral)',
    POSITIVE = 'var(--toast-positive)',
    NEGATIVE = 'var(--toast-negative)',
    MILD     = 'var(--toast-mild)',
    GRAY     = 'var(--toast-gray)',
}

type ColorValue = SafeStyle | ToastStyle | string;

export interface ToastParameters {
    header: string;
    content: string;
    icon?: IconDefinition | null;
    backgroundColor?: ColorValue;
    foregroundColor?: ColorValue;
} export class Toast {

    public header: string;
    public content: string;
    public icon: IconDefinition | null;
    public backgroundColor: ColorValue;
    public foregroundColor: ColorValue;

    constructor (
        { header, content, icon = faInfoCircle, backgroundColor = ToastStyle.NEUTRAL, foregroundColor = 'var(--card-fg)' }: ToastParameters
    ) {
        this.header = header;
        this.content = content;
        this.icon = icon;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }

}
