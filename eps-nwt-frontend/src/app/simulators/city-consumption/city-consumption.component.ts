import {Component, OnInit, ViewChild} from '@angular/core';
import {BaseChartDirective} from "ng2-charts";
import {BaseModule} from "../../base/base.module";
import {DatePipe, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute} from "@angular/router";
import {HouseholdService} from "../household.service";
import {WebSocketService} from "../../service/websocket.service";
import {
  Chart,
  BarElement,
  BarController,
  CategoryScale,
  LinearScale,
  Title,
  Tooltip,
  Legend,
  ChartType,
  ChartData, LineElement, PointElement, LineController
} from 'chart.js';
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-city-consumption',
  standalone: true,
  imports: [
    BaseChartDirective,
    BaseModule,
    DecimalPipe,
    FormsModule,
    NgIf,
    NgForOf,
    ReactiveFormsModule
  ],
  templateUrl: './city-consumption.component.html',
  styleUrl: './city-consumption.component.css'
})
export class CityConsumptionComponent implements OnInit{
  selected: boolean = false;
  citiesAndMunicipalities: any = {};
  cities: string[] = [];
  selectedCity: string = '';
  custom: boolean = false;
  timeRange = '3';
  startDate: string | undefined;
  endDate: string | undefined;
  chartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        label: 'Availability',
        data: [],
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }
    ]
  };
  chartType: ChartType = 'bar';
  @ViewChild(BaseChartDirective) chart: BaseChartDirective | undefined;

  constructor(private realEstateService: RealEstateRequestService,
              private http: HttpClient,
              private route: ActivatedRoute,
              private householdService: HouseholdService,
              private webSocketService: WebSocketService,
              private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.realEstateService.getCitiesWithMunicipalities().subscribe(data => {
      this.citiesAndMunicipalities = data;
      this.cities = Object.keys(this.citiesAndMunicipalities);
    });
  }

  onCityChange(event: Event) {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.selectedCity = target.value;
      this.selected = true;
    }
  }

  validateDateRange(start: Date, end: Date): boolean {
    if (end <= start) {
      return false;
    }
    const oneYear = 365 * 24 * 60 * 60 * 1000;  // milliseconds
    const difference = end.getTime() - start.getTime();
    return difference <= oneYear;
  }

  updateChart(): void {
    this.custom = this.timeRange === 'custom';
    if (this.startDate != undefined && this.endDate != undefined){
      if (this.validateDateRange(new Date(this.startDate), new Date(this.endDate))){
        // dalje
      }
      else {
        this.showSnackbar("The selected dates are invalid. The range must not exceed 1 year.");
        this.startDate = "";
        this.endDate = "";
      }
    }

    /*const name = "simulator-" + this.household?.id.toString();
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

    if (this.timeRange !== '1' && this.webSocketService.isConnected) {
      this.webSocketService.disconnect();
    }
    if (this.timeRange === '1') {
      const simulatorName = `${this.household?.id}`;
      this.initWebSocket(simulatorName);
    }

    this.fetchAvailabilityData(name, queryParam);*/
  }

  updateChartType(): void {
    if (this.chart) {
      this.chart.update();
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
