import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RequestViewAdminComponent } from './request-view-admin.component';

describe('RequestViewAdminComponent', () => {
  let component: RequestViewAdminComponent;
  let fixture: ComponentFixture<RequestViewAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RequestViewAdminComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RequestViewAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
