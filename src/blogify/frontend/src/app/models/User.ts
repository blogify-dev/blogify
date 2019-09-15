export class User{
  constructor(
    public username: string,
    public uuid: string
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
      public email: string
  ) {}
}
