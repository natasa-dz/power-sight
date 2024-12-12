import { Injectable } from "@angular/core";
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams, HttpResponse} from "@angular/common/http";
import {BehaviorSubject, map, Observable, of, throwError} from "rxjs";
import { catchError } from "rxjs/operators";
import {Role, User} from "../model/user.model";
import {JwtHelperService} from "@auth0/angular-jwt";
import {AuthResponse} from "../access-control-module/auth.service";
import {ChangePasswordDto} from "../model/change-password-dto.model";
import {RealEstateRequest} from "../model/real-estate-request.model";

@Injectable({
  providedIn: 'root',
})
export class UserService {

  private apiUrl = 'http://localhost:8080/users';

  private headers = new HttpHeaders({
      'Content-Type': 'application/json',
      skip: 'true',
  });

  user$ = new BehaviorSubject("");
  userState = this.user$.asObservable();


  userAccount$=new BehaviorSubject<User | null>(null);


  constructor(private http: HttpClient){
      this.user$.next(this.getRole());
      this.setUser();
      this.setUserDetails();

  }

  uploadPhoto(userId: number, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<string>(`${this.apiUrl}/${userId}/upload_photo`, formData);
  }

  getPhotoPath(userId: number): Observable<string> {
    return this.http.get<string>(`${this.apiUrl}/${userId}/photo`);
  }

  updateUser(dto: User): Observable<User> {
    const url = `${this.apiUrl}/${dto.username}`;
    return this.http.post<User>(url, dto).pipe(
      catchError((error: any) => {
        console.error('Error updating user:', error);
        return throwError(error);
      })
    );
  }

  setUserDetails(): void {
        this.getCurrentUser().subscribe(user => {
            this.userAccount$.next(user);
        });
    }

  registerUser(user: FormData): Observable<User> {
    const url = `${this.apiUrl}/register`;

    return this.http.post<User>(url, user).pipe(
      catchError((error: any) => {
        console.error('Error registering user:', error);
        return throwError(error);
      })
    );
  }


  private handleError(error: HttpErrorResponse) {
    console.error('Error:', error);
    return throwError('An error occurred. Please try again later.');
  }

  getUser(email: string): Observable<User> {
    const url = `${this.apiUrl}/${email}`;
    //{withCredentials:true}
    return this.http.get<User>(url).pipe(
      catchError((error: any) => {
        console.error('Error getting user:', error);
        return throwError(error);
      })
    );
  }

  getAllUsers(): Observable<User[]> {
    const url = `${this.apiUrl}`;
    //, {withCredentials:true}
    return this.http.get<User[]>(url).pipe(
      catchError((error: any) => {
        console.error('Error getting all users:', error);
        return throwError(error);
      })
    );
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
      console.log("User ID from getCurrentUser: ", userId);
      localStorage.setItem('username', userId);

    return this.getUser(userId);
  }

  isLoggedIn(): boolean {
      const user = localStorage.getItem('user');
      return user !== null && user !== '';
  }

  setUser(): void {
      this.user$.next(this.getRole());
  }


  getRole(): any {
      console.log("usao u getRole")
      if (this.isLoggedIn()) {
          console.log("is logged in")
          try {
              const accessToken: any = localStorage.getItem('user');
              const helper = new JwtHelperService();
              const decodedToken = helper.decodeToken(accessToken);
              console.log(decodedToken)

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

  activateAccount(token: string): Observable<HttpResponse<string>> {
    const params = new HttpParams().set('token', token);
    return this.http.patch<string>(`${this.apiUrl}/auth/activate`, null, { params, observe:'response' , responseType: 'text' as 'json'});
  }

  login(auth: any): Observable<AuthResponse> {
      return this.http.post<AuthResponse>(this.apiUrl + '/login', auth);
  }

  logout(): Observable<void|null> {

      localStorage.removeItem('user');
      localStorage.removeItem('userId');
      this.user$.next('');
      this.userAccount$.next(null);
      console.log("You have logged out successfully!");
      return of(null);
      // return this.http.get(environment.apiHost + 'users/login', {
      //   responseType: 'text',
  }

  changePassword(dto: ChangePasswordDto): Observable<HttpResponse<string>> {
    return this.http.post<string>(`${this.apiUrl}/change-password`, dto, {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      observe: 'response'
    });
  }

  getUserById(userId: number) : Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/byId/${userId}`)
  }
}
