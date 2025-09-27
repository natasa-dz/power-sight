import {Component, OnInit} from '@angular/core';
import {RealEstateRequest} from "../../model/real-estate-request.model";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {ActivatedRoute} from "@angular/router";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {User} from "../../model/user.model";
import {FormsModule} from "@angular/forms";
import {format} from "date-fns";
import {UserService} from "../../service/user.service";
import {FinishRealEstateRequestDTO} from "../../model/finish-real-estate-request-dto";
import {BaseModule} from "../../base/base.module";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-request-view-admin',
  standalone: true,
    imports: [
        NgIf,
        DatePipe,
        NgForOf,
        FormsModule,
        BaseModule
    ],
  templateUrl: './request-view-admin.component.html',
  styleUrl: './request-view-admin.component.css'
})
export class RequestViewAdminComponent implements OnInit{
  request: RealEstateRequest | undefined;
  owner: User | undefined;
  adminNote: string = '';
  createdAt: string = '';
  finishedAt: string = '';
  status: string = '';
  images: string[] = [];
  documentation: string[] = [];

  constructor(private service: RealEstateRequestService,
              private route: ActivatedRoute,
              private userService: UserService,
              private snackBar: MatSnackBar) {
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('requestId'));
    if (id !== null){
      this.service.getRequestForAdmin(id).subscribe({
        next: (data: RealEstateRequest) => {
          this.request = data;
          if (this.request.status !== undefined) {
            this.status = this.request.status.toString();
          }
          if (this.request.createdAt !== null) {
            this.createdAt = format(new Date(this.request.createdAt), 'dd.MM.yyyy.');
          } else{
            this.createdAt = 'Unknown';
          }
          if (this.request.finishedAt !== undefined && this.request.finishedAt !== null) {
            this.finishedAt = format(new Date(this.request.finishedAt), 'dd.MM.yyyy.');
          } else{
            this.finishedAt = 'Unknown';
          }

          this.userService.getUserById(this.request.owner).subscribe({
            next: (user: User) => {
              this.owner = user;
            },
            error: (_:any) => {
              console.log("Error fetching user with id " + this.request?.owner + "!")
            }
          });

          this.service.getImagesByRealEstateId(id).subscribe({
            next: (urls: string[]) => (this.images = urls),
            error: (err) => console.error('Error loading images', err)
          });

          this.service.getDocumentationByRealEstateId(id).subscribe({
            next: (urls: string[]) => (this.documentation = urls),
            error: (err) => console.error('Error loading docs', err)
          });
        },
        error: () => console.log("Error fetching request " + id + " for admin!")
      });
      }

  }

  finishRequest(approved: boolean) {
    let note = false;
    if (!note){
      if(!approved) {
        if (this.adminNote === '') {
          this.showSnackbar("Admin note is required for denied requests!\nAdd reason for denying the request.");
        } else {
          note = true;
        }
      } else {
        note = true;
      }

    }
    if (this.request?.id && note && this.owner?.username){
      let finishedRequest: FinishRealEstateRequestDTO = {
        owner: this.owner.username,
        approved: approved,
        note: this.adminNote
      }
      this.service.finishRequest(this.request.id, finishedRequest).subscribe({
        next: (message: string) => {
          this.showSnackbar(message)
          location.reload();
        },
        error: (mess:any) => {
          if(mess.status === 200){
            this.showSnackbar(mess.error.text);
            location.reload();
          } else{
            this.showSnackbar(mess.error.text);
            console.log("Error with finishing request!")
          }
        }
      });
    }
  }

  showSnackbar(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 4000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }
}
