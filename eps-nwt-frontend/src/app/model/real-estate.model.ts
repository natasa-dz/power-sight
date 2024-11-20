import {Household} from "./household.model";

export interface RealEstate {
  id: number;
  address: string;
  municipality: string;
  town: string;
  floors: number;
  images: string[];
  households: Household[];
}
