export class User {
  constructor (
    public uuid: string,
    public username: string,
    public name: string,
    public email: string,
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
