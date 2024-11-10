import {User} from "./user.model";
import {HouseholdRequest} from "./household-request.model";
import {RealEstateRequestStatus} from "../enum/real-estate-request-status";

export interface RealEstateRequest {
  id: number;
  owner: User;
  address: string;
  municipality: string;
  town: string;
  floors: string;
  images: string[];
  documentation: string[];
  status: RealEstateRequestStatus;
  householdRequests: HouseholdRequest[];
  createdAt: Date;
  approvedAt: Date | null;
  adminNote: string;
}
