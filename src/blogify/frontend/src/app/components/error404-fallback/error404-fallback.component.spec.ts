import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { Error404FallbackComponent } from './error404-fallback.component';

describe('Error404FallbackComponent', () => {
  let component: Error404FallbackComponent;
  let fixture: ComponentFixture<Error404FallbackComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ Error404FallbackComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(Error404FallbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
