import { TestBed } from '@angular/core/testing';

import { StaticContentService } from './static-content.service';
import { StaticFile } from '../../models/Static';

describe('StaticService', () => {
    beforeEach(() => TestBed.configureTestingModule({}));

    it('should be created', () => {
        const service: StaticContentService = TestBed.get(StaticContentService);
        expect(service).toBeTruthy();
    });

    it('should give the right URL', () => {
        const service: StaticContentService = TestBed.get(StaticContentService);

        let testId = 495697;
        let testCollectionName = 'testCollection';

        let givenUrl = service.urlFor(new StaticFile(testId, testCollectionName));

        expect(givenUrl).toMatch(new RegExp('.*\/api\/get\/testCollection\/495697'));
    });

});
