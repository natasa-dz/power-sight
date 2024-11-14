import {Component, OnInit} from '@angular/core';
import {HouseholdService} from "../household.service";
import {ActivatedRoute} from "@angular/router";
import {DatePipe, DecimalPipe, NgIf} from "@angular/common";
import {ViewHouseholdDto} from "../../model/view-household-dto.model";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-view-household',
  standalone: true,
  imports: [
    NgIf,
    DecimalPipe,
    FormsModule
  ],
  providers: [DatePipe],
  templateUrl: './view-household.component.html',
  styleUrl: './view-household.component.css'
})
export class ViewHouseholdComponent implements OnInit {
  household?: ViewHouseholdDto;
  onlinePercentage: string | undefined;
  offlinePercentage: string | undefined;
  onlineDuration: string | undefined;
  offlineDuration: string | undefined;
  timeRange = '3';
  startDate: string | undefined;
  endDate: string | undefined;

  constructor(
    private route: ActivatedRoute,
    private householdService: HouseholdService,
    private datePipe: DatePipe
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

  updateChart(): void {
    const name = "simulator-" + this.household?.id.toString();
    const timeRangeValue = this.timeRange;

    const formattedStartDate = this.startDate
      ? this.datePipe.transform(this.startDate, 'dd.MM.yyyy.')
      : '';
    const formattedEndDate = this.endDate
      ? this.datePipe.transform(this.endDate, 'dd.MM.yyyy.')
      : '';

    const queryParam =
      timeRangeValue === 'custom' && formattedStartDate && formattedEndDate
        ? `${formattedStartDate}-${formattedEndDate}`
        : timeRangeValue;

    this.fetchAvailabilityData(name, queryParam);
  }

  async fetchAvailabilityData(name: string, timeRange: string) {
    console.log(name, timeRange);
    this.householdService.getAvailability(name, timeRange).subscribe(
      (data: any) => {
        this.onlinePercentage = isNaN(data.onlinePercentage) ? '0' : data.onlinePercentage;
        this.offlinePercentage = isNaN(data.offlinePercentage) ? '0' : data.offlinePercentage;
        this.onlineDuration = isNaN(data.onlineDuration) ? '0' : data.onlineDuration;
        this.offlineDuration = isNaN(data.offlineDuration) ? '0' : data.offlineDuration;

      },
      (error) => {
        console.error("Error fetching household details", error);
      }
    );
  }
}
