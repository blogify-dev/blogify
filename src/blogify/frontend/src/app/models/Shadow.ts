import { Article } from '@blogify/models/Article';
import { User } from '@blogify/models/User';
import { Comment } from '@blogify/models/Comment';

export type Shadow<TResource extends Article | Comment | User> = TResource | string;

export function idOf<TResource extends Article | Comment | User>(shadow: Shadow<TResource>): string {
    return typeof shadow === 'object' ? shadow.uuid : shadow;
}

export function obj<TResource extends Article | Comment | User>(shadow: Shadow<TResource>): TResource | null {
    if (typeof shadow === 'object') return shadow;
    else return null;
}
