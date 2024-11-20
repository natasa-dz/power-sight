import {User} from "./user.model";
import {RealEstate} from "./real-estate.model";

export interface Household {
  id: number;
  floor: number;
  squareFootage: number;
  apartmentNumber: number;
  meters: object | null;
  realEstate: RealEstate;
  owner: User;
}
