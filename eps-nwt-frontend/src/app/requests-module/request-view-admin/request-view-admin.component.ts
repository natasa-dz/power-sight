import {Component, OnInit} from '@angular/core';
import {RealEstateRequest} from "../../model/real-estate-request.model";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {ActivatedRoute} from "@angular/router";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {User} from "../../model/user.model";
import {FormsModule} from "@angular/forms";
import {format} from "date-fns";
import {UserService} from "../../service/user.service";

@Component({
  selector: 'app-request-view-admin',
  standalone: true,
  imports: [
    NgIf,
    DatePipe,
    NgForOf,
    FormsModule
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

  constructor(private service: RealEstateRequestService,
              private route: ActivatedRoute,
              private userService: UserService) {
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
            next: (base64Images: string[]) => {
              this.images = base64Images;
            },
            error: (err) => {
              console.error('Error loading images', err);
            }
          });
        },
        error: (_:any) => {
          console.log("Error fetching request " + id + " for admin!")
        }
      });
    }

  }

  downloadDocument(filePath: string) {
    this.service.getDocumentBytes(filePath).subscribe({
      next: (fileBytes: ArrayBuffer) => {
        const blob = new Blob([fileBytes], { type: 'application/pdf' }); // Adjust MIME type as needed
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'request' + this.request?.id + '.pdf';
        link.click();
      },
      error: (err) => {
        console.error('Failed to download the document:', err);
      }
    });
  }

  saveAdminNote() {
    console.log("nista dugme")
  }
}
