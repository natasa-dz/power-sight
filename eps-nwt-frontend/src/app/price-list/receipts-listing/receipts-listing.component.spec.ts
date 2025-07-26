import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReceiptsListingComponent } from './receipts-listing.component';

describe('ReceiptsListingComponent', () => {
  let component: ReceiptsListingComponent;
  let fixture: ComponentFixture<ReceiptsListingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReceiptsListingComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReceiptsListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
