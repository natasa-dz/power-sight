import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateReceiptsComponent } from './create-receipts.component';

describe('CreateReceiptsComponent', () => {
  let component: CreateReceiptsComponent;
  let fixture: ComponentFixture<CreateReceiptsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateReceiptsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CreateReceiptsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
