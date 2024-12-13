import {Component, OnInit, ViewChild} from '@angular/core';
import {BaseChartDirective} from "ng2-charts";
import {BaseModule} from "../../base/base.module";
import {DatePipe, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RealEstateRequestService} from "../../service/real-estate-request.service";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute} from "@angular/router";
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
import {ConsumptionService} from "../consumption.service";
import {CityMunicipality} from "../../model/city-municipality";

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
  providers: [DatePipe],
  templateUrl: './city-consumption.component.html',
  styleUrl: './city-consumption.component.css'
})
export class CityConsumptionComponent implements OnInit{
  selected: boolean = false;
  citiesAndMunicipalities: any = {};
  cities: string[] = [];
  existingMunicipalities: string[] = [];
  existingCities: string[] = [];
  selectedCity: string = '';
  timeRange = '3';
  custom: boolean = false;
  startDate: string | undefined;
  endDate: string | undefined;
  total: string | undefined;
  chartData: ChartData<'bar'> = {
    labels: [],
    datasets: [
      {
        label: 'Energy Consumption',
        data: [],
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }
    ]
  };
  chartType: ChartType = 'bar';
  @ViewChild(BaseChartDirective) chart: BaseChartDirective | undefined;

  constructor(private consumptionService: ConsumptionService,
              private realEstateService: RealEstateRequestService,
              private http: HttpClient,
              private datePipe: DatePipe,
              private route: ActivatedRoute,
              private webSocketService: WebSocketService,
              private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.consumptionService.getMunicipalitiesFromInflux().subscribe(data => {
      this.existingMunicipalities = data; //novisad, novibeograd
      this.realEstateService.getCitiesWithMunicipalities().subscribe({
        next: (data2: CityMunicipality) => {
          this.citiesAndMunicipalities = data2; // Beograd - Novi Beograd, Zemun , ...
          this.cities = Object.keys(this.citiesAndMunicipalities);
          for (let municipality of this.existingMunicipalities){
            for (let city of this.cities){
              let municipalitiesHelper: string[] = this.citiesAndMunicipalities[city].map((item: string) => {
                return item.toLowerCase().replace(" ", '');
              });
              if (municipalitiesHelper.includes(municipality)){
                this.existingCities.push(city);
              }
            }
          }
        },
        error: (err: any): void => {
        }
      });
    });

    Chart.register(
      BarElement,
      BarController,
      CategoryScale,
      LinearScale,
      Title,
      Tooltip,
      Legend,
      LineController,
      PointElement,
      LineElement
    );
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
    let validDates = false;
    this.custom = this.timeRange === 'custom';
    const timeRangeValue = this.timeRange;

    const formattedStartDate = this.startDate
      ? this.datePipe.transform(this.startDate, 'dd.MM.yyyy.')
      : '';
    const formattedEndDate = this.endDate
      ? this.datePipe.transform(this.endDate, 'dd.MM.yyyy.')
      : '';

    const queryParam =
      timeRangeValue === 'custom' && formattedStartDate && formattedEndDate
        ? `${formattedStartDate} 00:00:00-${formattedEndDate} 00:00:00`
        : timeRangeValue;

    if (this.startDate != undefined && this.endDate != undefined){
      if (this.validateDateRange(new Date(this.startDate), new Date(this.endDate))){
        validDates = true;
      }
      else {
        validDates = false;
        this.showSnackbar("The selected dates are invalid. The range must not exceed 1 year.");
        this.startDate = "";
        this.endDate = "";
        this.total = undefined;
      }
    }

    // ako je custom i validni datumi ili ako nije custom
    if ((this.custom && validDates) || !this.custom){
      this.consumptionService.getConsumption(this.selectedCity, queryParam).subscribe({
        next: (data : number) => {
          if (data === null){
            this.total = "No data available";
          }
          else{
            this.total = Number(data.toFixed(4)).toString() + " kWh";
          }
          this.fetchGraphData(this.selectedCity, queryParam);
        },
        error: (mess:any) => {
          if(mess.status === 200){
            this.showSnackbar(mess.error.text);
          } else{
            this.showSnackbar("Error with consumption");
          }
        }
      });
    }
    /*
    if (this.timeRange !== '1' && this.webSocketService.isConnected) {
      this.webSocketService.disconnect();
    }
    if (this.timeRange === '1') {
      const simulatorName = `${this.household?.id}`;
      this.initWebSocket(simulatorName);
    }
*/

  }

  fetchGraphData(name: string, timeRange: string) {
    this.consumptionService.getGraphData(name, timeRange).subscribe(
      (graphData: any[]) => {
        const graphDataArray = Object.entries(graphData).map(([key, value]) => ({
          key,
          value,
        }));
        graphDataArray.sort((a, b) => {
          const isHour = /^\d{2}h$/.test(a.key); // Matches "08h", "21h"
          const isDate = /^\d{2}\.\d{2}\.\d{4}\.$/.test(a.key); // Matches "12.12.2023."
          const isWeek = /^\d{1,2}(st|nd|rd|th)$/.test(a.key); // Matches "1st", "2nd", "5th"

          if (isHour) {
            const hourA = parseInt(a.key.slice(0, 2), 10);
            const hourB = parseInt(b.key.slice(0, 2), 10);
            return hourA - hourB;
          } else if (isDate) {
            const dateA = new Date(a.key.split('.').reverse().join('-'));
            const dateB = new Date(b.key.split('.').reverse().join('-'));
            return dateA.getTime() - dateB.getTime();
          } else if (isWeek) {
            const weekA = parseInt(a.key.replace(/\D/g, ''), 10);
            const weekB = parseInt(b.key.replace(/\D/g, ''), 10);
            return weekA - weekB;
          } else {
            const monthMap = { JANUARY: 1, FEBRUARY: 2,
              MARCH: 3, APRIL: 4, MAY: 5, JUNE: 6, JULY: 7, AUGUST: 8, SEPTEMBER: 9,
              OCTOBER: 10, NOVEMBER: 11, DECEMBER: 12 };
            // @ts-ignore
            const monthA = monthMap[a.key];
            // @ts-ignore
            const monthB = monthMap[b.key];
            return monthA - monthB;
          }

        });

        this.chartData.labels = graphDataArray.map(item => item.key);
        this.chartData.datasets[0].data = graphDataArray.map(item => item.value);
        if (this.chart) {
          this.chart.update();
        }
      },
      (error) => {
        console.error("Error fetching graph data", error);
      }
    );
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
