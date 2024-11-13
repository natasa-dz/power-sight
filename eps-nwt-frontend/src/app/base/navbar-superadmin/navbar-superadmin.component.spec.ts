import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarSuperadminComponent } from './navbar-superadmin.component';

describe('NavbarSuperadminComponent', () => {
  let component: NavbarSuperadminComponent;
  let fixture: ComponentFixture<NavbarSuperadminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarSuperadminComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NavbarSuperadminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
