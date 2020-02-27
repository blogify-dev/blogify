import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileSlideoverComponent } from './profile-slideover.component';

describe('ProfileSlideoverComponent', () => {
  let component: ProfileSlideoverComponent;
  let fixture: ComponentFixture<ProfileSlideoverComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProfileSlideoverComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileSlideoverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
