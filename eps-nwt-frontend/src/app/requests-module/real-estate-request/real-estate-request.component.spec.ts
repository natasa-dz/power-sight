import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RealEstateRequestComponent } from './real-estate-request.component';

describe('RealEstateRequestComponent', () => {
  let component: RealEstateRequestComponent;
  let fixture: ComponentFixture<RealEstateRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RealEstateRequestComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RealEstateRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
