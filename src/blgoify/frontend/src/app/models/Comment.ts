import { Article } from './Article';

export interface Comment {
    commenter: string; // Can be user which isn't implemented yet
    article: Article; // Can be uuid string of the article. Don't know which one to use
    content: string;
    uuid: string;
}
