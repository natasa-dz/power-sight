import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OwnerHouseholdsListingComponent } from './owner-households-listing.component';

describe('OwnerHouseholdsListingComponent', () => {
  let component: OwnerHouseholdsListingComponent;
  let fixture: ComponentFixture<OwnerHouseholdsListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OwnerHouseholdsListingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OwnerHouseholdsListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
