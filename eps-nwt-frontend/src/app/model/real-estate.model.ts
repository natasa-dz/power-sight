import {Household} from "./household.model";

export interface RealEstate {
  id: number;
  address: string;
  municipality: string;
  town: string;
  floors: string;
  images: string[];
  households: Household[];
}
