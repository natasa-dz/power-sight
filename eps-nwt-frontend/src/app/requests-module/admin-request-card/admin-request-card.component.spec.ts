import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminRequestCardComponent } from './admin-request-card.component';

describe('AdminRequestCardComponent', () => {
  let component: AdminRequestCardComponent;
  let fixture: ComponentFixture<AdminRequestCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRequestCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AdminRequestCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
