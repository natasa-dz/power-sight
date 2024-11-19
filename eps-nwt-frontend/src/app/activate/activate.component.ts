import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../service/user.service";
import {HttpErrorResponse, HttpResponse} from '@angular/common/http';

@Component({
  selector: 'app-activate',
  templateUrl: './activate.component.html',
  styleUrls: ['./activate.component.css']
})
export class ActivateComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    console.log("Activate Component initialized!")
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      console.log("Token: ", token)
      if (token) {
        this.activateAccount(token);
      }
    });
  }
  activateAccount(token:string){
    console.log("Usao u acitvate Account!")
    this.userService.activateAccount(token).subscribe({
      next: (response: HttpResponse<string>) => {
        if (response.status === 200) {
          alert('Account activated successfully. You can now log in!');
          this.router.navigate(['login']);

        } else {
          alert('Unexpected response status: ' + response.status);
        }
      },
      error:(error:any)=>{
        console.error(error);
        if (error.status === 400) {
          alert('Activation failed: ' + error.error);
        } else {
          alert('An unexpected error occurred. Please try again later.');
        }
      }
    });
  }

}
