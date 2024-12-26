import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HouseholdRequestComponent } from './household-request.component';

describe('HouseholdRequestComponent', () => {
  let component: HouseholdRequestComponent;
  let fixture: ComponentFixture<HouseholdRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HouseholdRequestComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(HouseholdRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
