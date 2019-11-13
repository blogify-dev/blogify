import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SingleArticleBoxComponent } from './single-article-box.component';

describe('SingleArticleBoxComponent', () => {
  let component: SingleArticleBoxComponent;
  let fixture: ComponentFixture<SingleArticleBoxComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SingleArticleBoxComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SingleArticleBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
