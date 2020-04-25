import { StaticFile } from './Static';
import { IconDefinition } from '@fortawesome/fontawesome-common-types';

export type NotificationIcon = StaticFile | IconDefinition | null;

export interface Notification {
    icon: NotificationIcon;
    header: string;
    desc: string;
    routerLink: string;
}
