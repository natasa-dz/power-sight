import { TestBed } from '@angular/core/testing';

import { RealEstateRequestService } from './real-estate-request.service';

describe('RealEstateRequestService', () => {
  let service: RealEstateRequestService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RealEstateRequestService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
