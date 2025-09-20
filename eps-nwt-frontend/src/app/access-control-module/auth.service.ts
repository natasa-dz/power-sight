import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {BehaviorSubject, map, Observable, of, tap} from "rxjs";
import {JwtHelperService} from "@auth0/angular-jwt";
import {catchError} from "rxjs/operators";
import {UserService} from "../service/user.service";
import {Role, User} from "../model/user.model";
export const environment = {
  apiHost: 'http://localhost:8080/'
}
export interface AuthResponse {
  accessToken: string;
  expiresIn: number;
}

@Injectable({
  providedIn: 'root'
})

export class AuthService {

  private headers = new HttpHeaders({
    'Content-Type': 'application/json',
    skip: 'true',
  });

  //user$ = new BehaviorSubject(this.getRole());
  user$ = new BehaviorSubject("");
  userState = this.user$.asObservable();


  userAccount$=new BehaviorSubject<User | null>(null);

  constructor(private http: HttpClient, private userService:UserService) {
    this.user$.next(this.getRole());
    this.setUser();
    this.setUserDetails();
  }

  getRoleObservable(): Observable<string> {
    return this.userState;
  }

  login(auth: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${environment.apiHost}users/login`,
      auth,
      { headers: this.headers }
    );
  }

  logout(): Observable<void|null> {

    localStorage.removeItem('user');
    localStorage.removeItem('userId');
    this.user$.next('');
    this.userAccount$.next(null);
    return of(null);
    }


  getRole(): any {
    if (this.isLoggedIn()) {
      console.log("is logged in")
      try {
        const userToken: any = localStorage.getItem('user');

        const helper = new JwtHelperService();
        const decodedToken = helper.decodeToken(userToken);
        localStorage.setItem("userId", decodedToken.id);

        return decodedToken ? decodedToken.role : null;
      }
      catch (error) {
        alert("Error decoding token");
        return null;
      }
    }
    else{
      return Role.UNKNOWN;
    }
  }

  isLoggedIn(): boolean {
    const user = localStorage.getItem('user');
    return user !== null && user !== '';
  }

  setUser(): void {
    this.user$.next(this.getRole());
  }

  getCurrentUser(): Observable<User | null> {
    const accessToken = localStorage.getItem('user');

    if (!accessToken) {
      return of(null);
    }

    const helper = new JwtHelperService();
    const decodedToken = helper.decodeToken(accessToken);

    if (!decodedToken || !decodedToken.sub) {
      return of(null);
    }

    const userId = decodedToken.sub;
    if (!userId) {
      return of(null);
    }
    const allUsers$ = this.userService.getAllUsers();

    return allUsers$.pipe(
      map(users => {
        users.forEach(user=>console.log(user));

        const currentUser = users.find(user => user.username === userId);

        if (currentUser) {
            return currentUser;

        } else {
          return null;
        }
      }),

      catchError(error => {
        return of(null);
      })
    );
  }

  setUserDetails(): void {
    this.getCurrentUser().subscribe(user => {
      this.userAccount$.next(user);
    });
  }
}
