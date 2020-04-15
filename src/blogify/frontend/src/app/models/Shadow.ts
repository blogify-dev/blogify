import { Article } from './Article';
import { User } from './User';
import { Comment } from './Comment';

export type Shadow<TResource extends Article | Comment | User> = TResource | string;

export function idOf<TResource extends Article | Comment | User>(shadow: Shadow<TResource>): string {
    return typeof shadow === 'object' ? shadow.uuid : shadow;
}

export function obj<TResource extends Article | Comment | User>(shadow: Shadow<TResource>): TResource | null {
    if (typeof shadow === 'object') return shadow;
    else return null;
}
