import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddPriceListComponent } from './add-price-list.component';

describe('AddPriceListComponent', () => {
  let component: AddPriceListComponent;
  let fixture: ComponentFixture<AddPriceListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddPriceListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddPriceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
