import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CityConsumptionComponent } from './city-consumption.component';

describe('CityConsumptionComponent', () => {
  let component: CityConsumptionComponent;
  let fixture: ComponentFixture<CityConsumptionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CityConsumptionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CityConsumptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
