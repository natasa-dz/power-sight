import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchHouseholdComponent } from './search-household.component';

describe('SearchHouseholdComponent', () => {
  let component: SearchHouseholdComponent;
  let fixture: ComponentFixture<SearchHouseholdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchHouseholdComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SearchHouseholdComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
