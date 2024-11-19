import {User} from "./user.model";
import {HouseholdRequestDTO} from "./create-household-request-dto.model";
import {RealEstateRequestStatus} from "../enum/real-estate-request-status";

export interface RealEstateRequestDTO {
  owner: number;
  address: string;
  municipality: string;
  town: string;
  floors: number;
  images: File[] | null;
  documentation: File[] | null;
  status: RealEstateRequestStatus;
  householdRequests: HouseholdRequestDTO[];
  createdAt: Date;
  finishedAt: Date | null;
  adminNote: string;
}
