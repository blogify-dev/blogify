import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CoverPictureComponent } from './cover-picture.component';

describe('CoverPictureComponent', () => {
  let component: CoverPictureComponent;
  let fixture: ComponentFixture<CoverPictureComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CoverPictureComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CoverPictureComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
