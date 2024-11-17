import {Component, OnInit} from '@angular/core';
import {RealEstateRequest} from "../../model/real-estate-request.model";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-request-view-admin',
  standalone: true,
  imports: [],
  templateUrl: './request-view-admin.component.html',
  styleUrl: './request-view-admin.component.css'
})
export class RequestViewAdminComponent implements OnInit{
  request: RealEstateRequest | undefined;

  constructor(private service: RealEstateRequestService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('requestId'));
    if (id !== null){
      this.service.getRequestForAdmin(id).subscribe({
        next: (data: RealEstateRequest) => {
          this.request = data
        },
        error: (_:any) => {
          console.log("Error fetching request " + id + " for admin!")
        }
      });
    }
  }

}
