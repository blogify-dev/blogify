import { StaticFile } from '@blogify/models/Static';
import { Entity } from '@blogify/models/entities/Entity';

export class User implements Entity {
    
    constructor (
        public uuid: string,
        public username: string,
        public name: string,
        public email: string,
        public followers: string[],
        public isAdmin: boolean,
        public biography: string,
        public profilePicture: StaticFile,
        public coverPicture: StaticFile,
        public __type: string,
    ) {}

}

export interface LoginCredentials {
    username: string;
    password: string;
}

export class RegisterCredentials {
    constructor (
        public name: string,
        public username: string,
        public password: string,
        public email: string,
    ) {}
}
