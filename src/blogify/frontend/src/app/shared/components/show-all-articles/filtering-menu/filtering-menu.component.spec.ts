import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FilteringMenuComponent } from './filtering-menu.component';

describe('FilteringMenuComponent', () => {
  let component: FilteringMenuComponent;
  let fixture: ComponentFixture<FilteringMenuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FilteringMenuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilteringMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
