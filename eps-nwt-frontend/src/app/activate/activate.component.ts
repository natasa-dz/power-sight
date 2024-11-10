import { Component } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../service/user.service";

@Component({
  selector: 'app-activate',
  standalone: true,
  imports: [],
  templateUrl: './activate.component.html',
  styleUrl: './activate.component.css'
})
export class ActivateComponent {

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        this.activateAccount(token);
      }
    });
  }
  activateAccount(token:string){
    this.userService.activateAccount(token).subscribe({
      next:(response)=>{
        alert(response);
        this.router.navigate(['login']);
      },
      error:()=>{
        alert('Activation failed. Please check your token and try again! ')
      }
    });
  }

}
