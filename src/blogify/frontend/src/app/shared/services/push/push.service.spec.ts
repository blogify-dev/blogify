import { TestBed } from '@angular/core/testing';

import { PushService } from './push.service';
import { HttpClientTestingModule } from '@angular/common/http/testing'

describe('PushService', () => {
  let service: PushService;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [HttpClientTestingModule],
      providers: [PushService]});
    service = TestBed.inject(PushService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
