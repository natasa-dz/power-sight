import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarCitizenComponent } from './navbar-citizen.component';

describe('NavbarCitizenComponent', () => {
  let component: NavbarCitizenComponent;
  let fixture: ComponentFixture<NavbarCitizenComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarCitizenComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NavbarCitizenComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
