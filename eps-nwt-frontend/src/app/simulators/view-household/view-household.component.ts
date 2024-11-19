import {Component, OnDestroy, OnInit, Renderer2} from '@angular/core';
import {HouseholdService} from "../household.service";
import {ActivatedRoute} from "@angular/router";
import {DatePipe, DecimalPipe, NgIf} from "@angular/common";
import {ViewHouseholdDto} from "../../model/view-household-dto.model";
import {FormsModule} from "@angular/forms";
import { BaseChartDirective } from 'ng2-charts';
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
import { ViewChild } from '@angular/core';
import {WebSocketService} from "../../service/websocket.service";
import {BaseModule} from "../../base/base.module";


@Component({
  selector: 'app-view-household',
  standalone: true,
    imports: [
        NgIf,
        DecimalPipe,
        FormsModule,
        BaseChartDirective,
        BaseModule
    ],
  providers: [DatePipe],
  templateUrl: './view-household.component.html',
  styleUrl: './view-household.component.css'
})
export class ViewHouseholdComponent implements OnInit, OnDestroy {
  household?: ViewHouseholdDto;
  onlinePercentage: string | undefined;
  offlinePercentage: string | undefined;
  onlineDuration: string | undefined;
  offlineDuration: string | undefined;
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

  constructor(
    private route: ActivatedRoute,
    private householdService: HouseholdService,
    private datePipe: DatePipe,
    private webSocketService: WebSocketService
  ) {}

  ngOnInit(): void {
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

    const id = this.route.snapshot.paramMap.get('id');
    if (typeof id === "string") {
      localStorage.setItem("simulator-id", id);
    }
    if (id) {
      this.householdService.findById(+id).subscribe(
        (household) => {
          this.household = household;
          const simulatorName = `${this.household?.id}`;
          if (this.timeRange === '3') {
            this.initWebSocket(simulatorName);
          }
        },
        (error) => {
          console.error("Error fetching household details", error);
        }
      );
    }
    this.webSocketService.data$.subscribe(data => {
      this.updateChartSocket(data);
    });
  }

  ngOnDestroy(): void {
    if (this.webSocketService.client && this.webSocketService.isConnected) {
      this.webSocketService.disconnect();
    }
  }

  initWebSocket(simulatorId: string): void {
    console.log("Subscribing to WebSocket for simulator:", simulatorId);
    this.webSocketService.connect();
  }

  updateChartSocket(data: any): void {
    if (Array.isArray(data.data)) {
      var data2 = data.data;
      data2.sort((a: any, b: any) => {
        const hourA = parseInt(a.name.replace('h', ''), 10);
        const hourB = parseInt(b.name.replace('h', ''), 10);
        return hourA - hourB;
      });
      this.chartData.labels = data2.map((item: any) => item.name);
      this.chartData.datasets[0].data = data2.map((item: any) => item.availability);
      if (this.chart) {
        this.chart.update();
      }
    } else {
      console.error('Invalid data format:', data);
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

    if (this.timeRange !== '3' && this.webSocketService.isConnected) {
      this.webSocketService.disconnect();
    }
    if (this.timeRange === '3') {
      const simulatorName = `${this.household?.id}`;
      this.initWebSocket(simulatorName);
    }

    this.fetchAvailabilityData(name, queryParam);
  }

  async fetchAvailabilityData(name: string, timeRange: string) {
    this.householdService.getAvailability(name, timeRange).subscribe(
      (data: any) => {
        this.onlinePercentage = isNaN(data.onlinePercentage) ? '0' : data.onlinePercentage;
        this.offlinePercentage = isNaN(data.offlinePercentage) ? '0' : data.offlinePercentage;
        this.onlineDuration = isNaN(data.onlineDuration) ? '0' : data.onlineDuration;
        this.offlineDuration = isNaN(data.offlineDuration) ? '0' : data.offlineDuration;

        this.fetchGraphData(name, timeRange);
      },
      (error) => {
        console.error("Error fetching household details", error);
      }
    );
  }

  fetchGraphData(name: string, timeRange: string) {
    this.householdService.getGraphData(name, timeRange).subscribe(
      (graphData: any[]) => {
        graphData.sort((a, b) => {
          const isHour = /^\d{2}h$/.test(a.name); // Matches "08h", "21h"
          const isDate = /^\d{2}\.\d{2}\.\d{4}\.$/.test(a.name); // Matches "12.12.2023."
          const isWeek = /^\d{1,2}st$/.test(a.name); // Matches "1st", "2nd", "5th"
          const isMonth = /^[A-Za-z]{3}$/.test(a.name); // Matches "Jan", "Feb", etc.

          if (isHour) {
            const hourA = parseInt(a.name.slice(0, 2), 10);
            const hourB = parseInt(b.name.slice(0, 2), 10);
            return hourA - hourB;
          } else if (isDate) {
            const dateA = new Date(a.name.split('.').reverse().join('-'));
            const dateB = new Date(b.name.split('.').reverse().join('-'));
            return dateA.getTime() - dateB.getTime();
          } else if (isWeek) {
            const weekA = parseInt(a.name.replace(/\D/g, ''), 10);
            const weekB = parseInt(b.name.replace(/\D/g, ''), 10);
            return weekA - weekB;
          } else if (isMonth) {
            const monthMap = { Jan: 1, Feb: 2, Mar: 3, Apr: 4, May: 5, Jun: 6, Jul: 7, Aug: 8, Sep: 9, Oct: 10, Nov: 11, Dec: 12 };
            // @ts-ignore
            const monthA = monthMap[a.name];
            // @ts-ignore
            const monthB = monthMap[b.name];
            return monthA - monthB;
          }

          return 0;
        });

        this.chartData.labels = graphData.map(item => item.name);
        this.chartData.datasets[0].data = graphData.map(item => item.availability);
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

}
