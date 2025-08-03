export interface PriceList {
  id?: number;
  startDate?: Date;
  endDate?: Date | null;
  greenZone: number;
  blueZone: number;
  redZone: number;
  basePrice: number;
  pdvPercentage: number;
}
