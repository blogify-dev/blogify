import { StaticFile } from './Static';
import { IconDefinition } from '@fortawesome/fontawesome-common-types';

export interface Notification {
    icon: StaticFile | IconDefinition | null;
    header: string;
    desc: string;
    routerLink: string;
}
