import { TestBed } from '@angular/core/testing'

import { StateService } from './state.service'
import { Article } from '../../../models/Article'
import { User } from '../../../models/User'
import { HttpClientTestingModule } from '@angular/common/http/testing'

describe('StateService', () => {
    let service: StateService
    const mockUser: User = {
        uuid: '3bb8e951-1bfa-4a53-b3f3-065b406e98ff',
        username: 'user',
        name: 'user',
        email: 'mail@bruh.com',
        followers: [],
        isAdmin: false,
        profilePicture: null,
        coverPicture: null,
    }

    const mockArticle: Article = {
        uuid: 'c8b483ce-5225-4ad0-aac7-9d78830e0826',
        title: 'some text',
        content: 'some text',
        summary: 'some text',
        createdBy: mockUser,
        createdAt: Date.now(),
        isPinned: false,
        categories: [{ name: 'some text' }],
        likedByUser: true,
        likeCount: 1,
        commentCount: 10,
    }

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [StateService],
        })
        service = TestBed.inject(StateService)

        service.clearArticles()
        service.clearUsers()
    })

    it('should be created', () => {
        expect(service).toBeTruthy()
    })

    it('should cache an uncached article', () => {
        const fromCacheBefore = service.getArticle(mockArticle.uuid)
        expect(fromCacheBefore).toBeNull()

        service.cacheArticle(mockArticle)

        const fromCacheAfter = service.getArticle(mockArticle.uuid)
        expect(fromCacheAfter).toBe(mockArticle)
    })


})
