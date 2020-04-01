import { StaticFile } from './Static';

export class User {
  constructor (
    public uuid: string,
    public username: string,
    public name: string,
    public email: string,
    public followers: string[],
    public isAdmin: boolean,
    public profilePicture: StaticFile,
    public coverPicture: StaticFile
  ) {}
}

export class LoginCredentials {
  constructor(
    public username: string,
    public password: string,
  ) {}
}

export class RegisterCredentials {
  constructor(
      public name: string,
      public username: string,
      public password: string,
      public email: string,
  ) {}
}
