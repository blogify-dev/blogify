export interface EventPayload {
    e: string;
    t: 'Activity' | 'Notification';
    d: object;
}

export interface CommentCreatePayload {
    article: string;
    commenter: string;
    comment: string;
}
