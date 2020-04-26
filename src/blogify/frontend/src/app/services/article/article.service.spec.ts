import { TestBed } from '@angular/core/testing'

import { ArticleService } from './article.service'
import { HttpClientTestingModule } from '@angular/common/http/testing'

describe('ArticleService', () => {
    beforeEach(() => TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
        providers: [ArticleService],
    }))

    it('should be created', () => {
        const service: ArticleService = TestBed.inject(ArticleService)
        expect(service).toBeTruthy()
    })
})
