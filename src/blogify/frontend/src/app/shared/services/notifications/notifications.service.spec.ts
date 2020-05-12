import { TestBed } from '@angular/core/testing';

import { NotificationsService } from './notifications.service';
import { HttpClientTestingModule } from '@angular/common/http/testing'

describe('NotificationsService', () => {
  let service: NotificationsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [NotificationsService]
    });
    service = TestBed.inject(NotificationsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
