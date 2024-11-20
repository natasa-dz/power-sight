import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OwnerRequestListingComponent } from './owner-request-listing.component';

describe('OwnerRequestListingComponent', () => {
  let component: OwnerRequestListingComponent;
  let fixture: ComponentFixture<OwnerRequestListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OwnerRequestListingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OwnerRequestListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
