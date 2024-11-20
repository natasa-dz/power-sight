import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OwnerRequestCardComponent } from './owner-request-card.component';

describe('OwnerRequestCardComponent', () => {
  let component: OwnerRequestCardComponent;
  let fixture: ComponentFixture<OwnerRequestCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OwnerRequestCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OwnerRequestCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
