import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HouseholdConsumptionComponent } from './household-consumption.component';

describe('HouseholdConsumptionComponent', () => {
  let component: HouseholdConsumptionComponent;
  let fixture: ComponentFixture<HouseholdConsumptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HouseholdConsumptionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(HouseholdConsumptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
