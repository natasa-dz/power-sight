import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchCitizensComponent } from './search-citizens.component';

describe('SearchCitizensComponent', () => {
  let component: SearchCitizensComponent;
  let fixture: ComponentFixture<SearchCitizensComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchCitizensComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SearchCitizensComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
