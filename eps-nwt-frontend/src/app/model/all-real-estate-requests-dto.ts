import {RealEstateRequestStatus} from "../enum/real-estate-request-status";

export interface AllRealEstateRequestsDto{
  id? : number;
  owner : number;
  status? : RealEstateRequestStatus;
  createdAt: Date | null;
  approvedAt: Date | null;
  address: string;
  municipality: string;
  town: string;
}
