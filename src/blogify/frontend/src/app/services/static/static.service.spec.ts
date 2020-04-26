import { TestBed } from '@angular/core/testing';

import { StaticContentService } from './static-content.service';
import { StaticFile } from '../../models/Static';
import { HttpClientTestingModule } from '@angular/common/http/testing'

describe('StaticService', () => {
    beforeEach(() => TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [StaticContentService],
    }));

    it('should be created', () => {
        const service: StaticContentService = TestBed.inject(StaticContentService);
        expect(service).toBeTruthy();
    });

    it('should give the right URL', () => {
        const service: StaticContentService = TestBed.get(StaticContentService);

        let testId = '495697';

        let givenUrl = service.urlFor(new StaticFile(testId));

        expect(givenUrl).toMatch(new RegExp('.*\/api\/get\/495697'));
    });

});
