import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ShowAllArticlesComponent } from './show-all-articles.component';

describe('ShowAllArticlesComponent', () => {
  let component: ShowAllArticlesComponent;
  let fixture: ComponentFixture<ShowAllArticlesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ShowAllArticlesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ShowAllArticlesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
