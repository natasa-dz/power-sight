import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../service/user.service";

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
      next:(response)=>{
        alert(response);
        alert('Activation successful. You can now login into your account! ')
        //this.router.navigate(['login']);
      },
      error:()=>{
        alert('Activation failed. Please check your token and try again! ')
      }
    });
  }

}
