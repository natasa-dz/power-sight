import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OwnerHouseholdRequestsComponent } from './owner-household-requests.component';

describe('OwnerHouseholdRequestsComponent', () => {
  let component: OwnerHouseholdRequestsComponent;
  let fixture: ComponentFixture<OwnerHouseholdRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OwnerHouseholdRequestsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OwnerHouseholdRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
