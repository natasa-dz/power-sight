import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminHouseholdRequestsComponent } from './admin-household-requests.component';

describe('AdminHouseholdRequestsComponent', () => {
  let component: AdminHouseholdRequestsComponent;
  let fixture: ComponentFixture<AdminHouseholdRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminHouseholdRequestsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminHouseholdRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
