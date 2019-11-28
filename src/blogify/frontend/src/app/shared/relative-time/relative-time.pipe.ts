import { Pipe, PipeTransform } from '@angular/core';

const milliSecondsInDay = 1000 * 3600 * 24;
const milliSecondsInHour = 1000 * 3600;
const milliSecondsInMinute = (1000 * 3600) / 60;

// Cast as any because typescript typing haven't updated yet
// tslint:disable-next-line:no-any
const rtf = new (Intl as any).RelativeTimeFormat('en');

/**
 * Custom pipe to get relative time in days, hours or minutes
 */
@Pipe({
    name: 'relativeTime'
})
export class RelativeTimePipe implements PipeTransform {

    /**
     * Transform function for `relativeTime` pipe
     * @param epochTime a unix timestamp
     * If the difference is less than a minute, it returns: 'Just now'
     * @return relative difference
     */
    transform(epochTime: number): string {
        const diffInMilliseconds = (epochTime * 1000) - new Date().getTime();
        const formattedDays = rtf.format(Math.round(diffInMilliseconds / milliSecondsInDay), 'day');

        const formattedHour = formattedDays !== '0 days ago' ? formattedDays :
            rtf.format(Math.round(diffInMilliseconds / milliSecondsInHour), 'hour');

        const formattedMinute = formattedHour !== '0 hours ago' ? formattedHour :
            rtf.format(Math.round(diffInMilliseconds / milliSecondsInMinute), 'minutes');

        return (formattedMinute !== '0 minutes ago') ? formattedMinute : 'Just now';
    }

}
