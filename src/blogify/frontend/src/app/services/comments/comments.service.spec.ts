import { TestBed } from '@angular/core/testing'

import { CommentsService } from './comments.service'
import { HttpClientTestingModule } from '@angular/common/http/testing'

describe('CommentsService', () => {
    beforeEach(() => TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [CommentsService],
    }))

    it('should be created', () => {
        const service: CommentsService = TestBed.inject(CommentsService)
        expect(service).toBeTruthy()
    })
})
