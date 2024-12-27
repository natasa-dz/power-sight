export interface HouseholdAccessDto {
  id: number;
  floor: number;
  squareFootage: number;
  apartmentNumber: number;
  ownerId: number | null;
  address: string;
  municipality: string;
  town: string;
  accessGranted: number[];
}
