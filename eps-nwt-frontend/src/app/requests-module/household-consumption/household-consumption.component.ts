import {ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {BaseModule} from "../../base/base.module";
import {CommonModule, DatePipe, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {HouseholdService} from "../../simulators/household.service";
import {Page} from "../../model/page.model";
import {HouseholdDto} from "../../model/householdDTO";

import {
    BarController,
    BarElement,
    CategoryScale,
    Chart,
    ChartData,
    ChartType,
    Legend,
    LinearScale,
    LineController,
    LineElement,
    PointElement,
    Title,
    Tooltip
} from 'chart.js';
import {WebSocketService} from "../../service/websocket.service";
import {BaseChartDirective} from "ng2-charts";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute} from "@angular/router";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ConsumptionService} from "../../simulators/consumption.service";


@Component({
  selector: 'app-household-consumption',
  standalone: true,
  imports: [
    BaseModule,
    CommonModule,
    FormsModule,
    BaseChartDirective,
    NgIf,
    NgForOf,
    DecimalPipe
  ],
  providers: [DatePipe],
  templateUrl: './household-consumption.component.html',
  styleUrl: './household-consumption.component.css'
})
export class HouseholdConsumptionComponent implements OnInit {
  page: Page<HouseholdDto> = {
    content: [],
    totalPages: 0,
    totalElements: 0,
    size: 10, // Default page size
    number: 0
  };

  currentPage: number = 1; // 1-based indexing for display
  selectedHouseholdId: number | null = null;
  userId: number | null = null;
  selected: boolean | undefined;
  timeRange: string = "1"; // Default time range: Last Hour
  custom: boolean = false; // Indicates if custom date range is selected
  startDate: string | null = null; // Start date for custom range
  endDate: string | null = null; // End date for custom range
  households: HouseholdDto[] = []; // Array to store the list of households
  total: string|undefined; // Total consumption for selected range

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

  constructor(
    private householdService: HouseholdService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private datePipe: DatePipe,
    private route: ActivatedRoute,
    private webSocketService: WebSocketService,
    private consumptionService: ConsumptionService,
  private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.userId = this.getUserIdFromLocalStorage();
    if (this.userId !== null) {
      this.loadHouseholds();
    } else {
      console.error("User ID is not available.");
    }

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

  onSelectHousehold(event: any): void {
      if (event.target && event.target.value) {
          let old = this.selectedHouseholdId;
          this.selectedHouseholdId = parseInt(event.target.value, 10);
          this.selected = true;
          this.startDate = "";
          this.endDate = "";
          this.total = undefined;
          this.timeRange = '';
          this.chartData.labels = []
          this.chartData.datasets[0].data = []
          if (old !== null) this.disconnectWebSocket(old);
      }
  }

  disconnectWebSocket(householdId : number): void {
    if (this.webSocketService.client && this.webSocketService.isConnectedHouse.get(householdId)) {
      this.webSocketService.disconnectHouse(householdId);
    }
  }
  initWebSocket(householdId: number): void {
    console.log("Subscribing to WebSocket for household:", householdId);
    this.webSocketService.connectHouse(householdId);
  }

  getUserIdFromLocalStorage(): number | null {
    const userIdString = localStorage.getItem('userId');
    return userIdString ? parseInt(userIdString, 10) : null;
  }

  loadHouseholds(): void {
    if (this.userId === null) {
      console.error("Cannot fetch households without a valid user ID.");
      return;
    }

    this.householdService
      .getOwnerHouseholds(this.userId, this.currentPage - 1, this.page.size) // Adjust for 0-based index
      .subscribe({
        next: (response) => {
          this.page = response;
            // Append new households to the list
            if (this.currentPage === 1) {
                this.households = response.content; // Replace on the first page
            } else {
                this.households = [...this.households, ...response.content]; // Append on subsequent pages
            }
          this.cdr.detectChanges(); // Ensure updates are applied
        },
        error: (err) => {
          console.error("Failed to fetch owner households:", err);
        }
      });
  }


    onPageChange(): void {
        if (this.currentPage < this.page.totalPages) {
            this.currentPage++;
            this.loadHouseholds();
        } else {
            console.log("No more pages to load.");
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

  updateChartSocket(data: Map<string,number>): void { //{k=v}
    this.chartData.labels = Object.keys(data);
    this.chartData.datasets[0].data = Object.values(data);
    if (this.chart) {
      this.chart.update();
    }

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

    if (this.custom && this.startDate != undefined && this.endDate != undefined){
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

    if (this.timeRange !== '1' && this.webSocketService.isConnectedHouse.get(this.selectedHouseholdId!)) {
      console.log("Usao u disconnect")
      this.webSocketService.disconnectHouse(this.selectedHouseholdId!);
    }

    if (this.timeRange === '1') {
      console.log("Usao u init")

      this.initWebSocket(this.selectedHouseholdId!);
    }
    this.webSocketService.houseData$.subscribe(data => {
      console.log("Usao u update")

      this.updateChartSocket(data);
    });

    // ako je custom i validni datumi ili ako nije custom
    if ((this.custom && validDates) || !this.custom){
      console.log("Query param: ",queryParam)
      this.consumptionService.getHouseholdConsumption(this.selectedHouseholdId!, queryParam).subscribe({
        next: (data : any) => {
          if (data === null){
            this.total = "No data available";
          }
          else{
            console.log("Data: ", data)
            this.total = Number(data.toFixed(4)).toString() + " kWh";
          }
          this.fetchGraphData(this.selectedHouseholdId!, queryParam);
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

  }

  fetchGraphData(householdId: number, timeRange: string) {
    this.consumptionService.getGraphHouseholdConsumption(householdId, timeRange).subscribe(
      (graphData: any[]) => {

        if (!graphData || graphData.length === 0) {
          console.log("No data received. Setting consumption to 0.");

          // Set chart data to 0 if no data is received
          this.chartData.labels = ['No Data'];  // You can adjust this to suit your needs
          this.chartData.datasets[0].data = [0];  // Set consumption data to 0

          if (this.chart) {
            this.chart.update();
          }
          return;  // Exit early if no data
        }

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

