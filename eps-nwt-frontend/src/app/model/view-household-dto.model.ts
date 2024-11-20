import {User} from "./user.model";
import {RealEstate} from "./real-estate.model";

export interface ViewHouseholdDto {
  id: number;
  floor: number;
  squareFootage: number;
  apartmentNumber: number;
  ownerId: number | null;
  address: string;
  municipality: string;
  town: string;
}
