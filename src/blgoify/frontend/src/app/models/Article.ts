import { Content } from './Content';
import { User } from './User';

export interface Article {
  title: string;
  createdAt: string;
  createdBy: User;
  content: Content;
  uuid: string;
}


