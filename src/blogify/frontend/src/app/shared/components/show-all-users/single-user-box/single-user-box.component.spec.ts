import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SingleUserBoxComponent } from './single-user-box.component';

describe('SingleUserBoxComponent', () => {
  let component: SingleUserBoxComponent;
  let fixture: ComponentFixture<SingleUserBoxComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SingleUserBoxComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SingleUserBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
