export class UsernamePasswordCredentials {
    constructor(
        public username: string,
        public password: string,
    ) {}
}

export class User {
    constructor(
        public uuid: string,
        public username: string,
        public info: string,
    ) {}
}