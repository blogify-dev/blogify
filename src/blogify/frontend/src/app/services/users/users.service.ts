import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from "../../models/User";

@Injectable({
  providedIn: 'root'
})
export class UsersService {
  private usersEndpoint = '/api/users';

  constructor(private httpClient: HttpClient) { }

  getUser(uuid: string) {
    return this.httpClient.get<User>(`${this.usersEndpoint}/${uuid}`);
  }

  /* Temporary */
  getAllUsers() {
    return this.httpClient.get(this.usersEndpoint);
  }
}
