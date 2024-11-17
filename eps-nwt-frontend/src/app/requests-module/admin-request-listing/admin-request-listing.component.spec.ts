import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRequestListingComponent } from './admin-request-listing.component';

describe('AdminRequestListingComponent', () => {
  let component: AdminRequestListingComponent;
  let fixture: ComponentFixture<AdminRequestListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRequestListingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminRequestListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
