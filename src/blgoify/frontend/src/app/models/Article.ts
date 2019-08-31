import { Content } from './Content';

export interface Article {
  title: string;
  createdAt: string;
  createdBy: string;
  content: Content;
  uuid: string;
}


