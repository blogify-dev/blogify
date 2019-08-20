export interface Article {
  title: string;
  createdAt: string;
  createdBy: string;
  content: Content;
  uuid: string;
}

export interface Content {
  text: string;
  summary: string;
}
