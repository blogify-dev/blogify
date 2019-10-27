import { TestBed } from '@angular/core/testing';

import { UsernameResolver } from './username-resolver';

describe('UsernameResolverService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UsernameResolver = TestBed.get(UsernameResolver);
    expect(service).toBeTruthy();
  });
});
