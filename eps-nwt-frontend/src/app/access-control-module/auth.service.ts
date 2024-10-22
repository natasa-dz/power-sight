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

  login(auth: any, recaptchaToken: string): Observable<AuthResponse> {
    const body = {
      ...auth,
      recaptchaToken: recaptchaToken
    };
    return this.http.post<AuthResponse>(`${environment.apiHost}users/login`, body);
  }

  logout(): Observable<void|null> {

    localStorage.removeItem('user');
    this.user$.next('');
    this.userAccount$.next(null);
    console.log("You have logged out successfully!");
    return of(null);
    // return this.http.get(environment.apiHost + 'users/login', {
    //   responseType: 'text',
    }

  verifyOtp(email: string, otp: string): Observable<AuthResponse> {
    const body = { email, otp };
    return this.http.post<AuthResponse>(`${environment.apiHost}users/verify-otp`, body);
  }



  // logOut(): Observable<any> {
  //   return this.http.post(environment.apiHost + 'users/logout', {}).pipe(
  //     tap(() => {
  //       console.log("You have loggedOut successfully!")
  //       // Clear user-related data on logout
  //       localStorage.removeItem('user');
  //       this.user$.next('');
  //       this.userAccount$.next(null);
  //     })
  //   );
  // }



  getRole(): any {
    console.log("usao u getRole")
    if (this.isLoggedIn()) {
      console.log("is logged in")
      try {
        const accessToken: any = localStorage.getItem('user');
        const helper = new JwtHelperService();
        const decodedToken = helper.decodeToken(accessToken);
        console.log(decodedToken)

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
    console.log(accessToken);
    if (!accessToken) {
      return of(null);
    }

    const helper = new JwtHelperService();
    const decodedToken = helper.decodeToken(accessToken);

    if (!decodedToken || !decodedToken.sub) {
      return of(null);
    }

    const userId = decodedToken.sub;
    console.log("User ID: ", userId);
    //return userId;
    if (!userId) {
      console.error('User ID not available in the decoded token');
      return of(null);
    }
    const allUsers$ = this.userService.getAllUsers();

    // Use RxJS map operator to transform the result
    return allUsers$.pipe(
      map(users => {
        users.forEach(user=>console.log(user));

        // Find the user with the matching ID
        const currentUser = users.find(user => user.username === userId);

        if (currentUser) {
          // Check if the user is blocked
          // if (currentUser.isBlocked) {
          //   console.log('User is blocked.');
          //   // Optionally, you can log the user out or redirect to a different page
          //   return null;
          // } else {

            return currentUser;

        } else {
          console.error('User not found with ID:', userId);
          return null;
        }
      }),

      catchError(error => {
        console.error('Error fetching user info---> getCurrentUser()', error);
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
