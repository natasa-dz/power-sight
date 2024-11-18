import {RealEstateRequestStatus} from "../enum/real-estate-request-status";
import {HouseholdRequest} from "./household-request.model";

export interface RealEstateRequest {
  id: number;
  owner: number;
  address: string;
  municipality: string;
  town: string;
  floors: number;
  images: File[] | null;
  documentation: string[] | null;
  status: RealEstateRequestStatus;
  householdRequests: HouseholdRequest[];
  createdAt: Date;
  finishedAt: Date | null;
  adminNote: string;
}
