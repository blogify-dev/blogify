import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserDisplayComponent } from './user-display.component';

describe('UserDisplayComponent', () => {
  let component: UserDisplayComponent;
  let fixture: ComponentFixture<UserDisplayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserDisplayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
