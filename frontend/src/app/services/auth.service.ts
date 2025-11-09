import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor() {
    // Zatím fake user pro development
    const fakeUser: User = {
      id: 1,
      username: 'demo',
      email: 'demo@example.com',
      firstName: 'Demo',
      lastName: 'User',
      role: 'USER'
    };

    this.currentUserSubject = new BehaviorSubject<User | null>(fakeUser);
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(username: string, password: string): Observable<User> {
    // Zatím fake login - později implementuji JWT
    const fakeUser: User = {
      id: 1,
      username: username,
      email: 'demo@example.com',
      firstName: 'Demo',
      lastName: 'User',
      role: 'USER'
    };

    this.currentUserSubject.next(fakeUser);
    return new Observable(observer => {
      observer.next(fakeUser);
      observer.complete();
    });
  }

  logout(): void {
    this.currentUserSubject.next(null);
  }

  isLoggedIn(): boolean {
    return this.currentUserValue !== null;
  }
}
