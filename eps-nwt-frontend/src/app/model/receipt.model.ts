import {Household} from "./household.model";
import {PriceList} from "./price-list";

export interface Receipt {
  id: number;
  priceList: PriceList;
  householdId: number;
  householdAddress: string;
  householdApartmentNumber: number;
  ownerId: number;
  ownerUsername: string;
  price: number;
  greenZoneConsumption: number;
  blueZoneConsumption: number;
  redZoneConsumption: number;
  paid: boolean;
  paymentDate: Date | null;
  path: string;
  month: string;
  year: number;
}
