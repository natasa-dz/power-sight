import {Component, OnInit} from '@angular/core';
import {HouseholdService} from "../household.service";
import {ActivatedRoute} from "@angular/router";
import {Household} from "../../model/household.model";
import {NgIf} from "@angular/common";
import {ViewHouseholdDto} from "../../model/view-household-dto.model";

@Component({
  selector: 'app-view-household',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './view-household.component.html',
  styleUrl: './view-household.component.css'
})
export class ViewHouseholdComponent implements OnInit {
  household?: ViewHouseholdDto;

  constructor(
    private route: ActivatedRoute,
    private householdService: HouseholdService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.householdService.findById(+id).subscribe(
        (household) => {
          this.household = household;
        },
        (error) => {
          console.error("Error fetching household details", error);
        }
      );
    }
  }
}
