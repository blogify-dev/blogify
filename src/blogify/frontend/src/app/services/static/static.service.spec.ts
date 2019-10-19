import { TestBed } from '@angular/core/testing';

import { StaticContentService } from './static-content.service';

describe('StaticService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: StaticContentService = TestBed.get(StaticContentService);
    expect(service).toBeTruthy();
  });
});
