describe('Comments tests', () => {
    beforeEach(() => {
        // @ts-ignore
        cy.resetArticles();
        window.localStorage.clear();
        cy.viewport(1920, 1080);
    });


    it('Should create parent comment', () => {
        // @ts-ignore
        cy.login();
        // @ts-ignore
        cy.createArticle(1).then(resp => {
            const uuid = resp.body.created[0];
            cy.visit(`/article/${uuid}`);
            const text = 'Comment reply';
            cy.get('#article-reply')
                .get('.comment-reply')
                .get('.reply-text')
                .type(text);

            cy.get('button')
                .contains('Submit')
                .click();

            cy.get('#root-comments')
                .children().first()
                .get('.comment')
                .get('.body')
                .should('contain.text', text);

        });
    });

    it('Submit comment button should not be clickable when logged out', () => {
        // @ts-ignore
        cy.createArticle(1).then(resp => {
            const uuid = resp.body.created[0];
            cy.visit(`/article/${uuid}`);

            cy.get('button')
                .contains('Submit')
                .should('have.class', 'disabled');
        });
    });

    it('Should delete created comment', () => {
        // @ts-ignore
        cy.login();
        // @ts-ignore
        cy.createArticleWithParentComment(1).then(resp => {
            cy.visit(`/article/${resp.article}`);
            cy.get('#root-comments')
                .get('app-single-comment')
                .first()
                .get('.comment .buttons')
                .contains('Delete')
                .click();
            cy.get('#delete-confirmation')
                .get('span')
                .contains('Yes')
                .click();
            cy.get('#root-comments')
                .children()
                .should('have.length', 0);
        });
    });

    it('Should create child comment', () => {
        // @ts-ignore
        cy.login();
        // @ts-ignore
        cy.createArticleWithParentComment(1).then(resp => {
            cy.visit(`/article/${resp.article}`);
            cy.get('#root-comments')
                .get('app-single-comment')
                .first()
                .get('.comment')
                .get('.buttons')
                .get('.reply')
                .click();

            const text = 'A reply';

            cy.get('app-create-comment')
                .last()
                .get('.reply textarea')
                .type(text);

            cy.get('app-create-comment')
                .last()
                .get('.reply .reply-submit')
                .contains('Submit')
                .click();

            cy.get('#root-comments')
                .children().first()
                .get('div .children')
                .first()
                .should('contain.text', text);
        });
    });

    it('Should like comment', () => {
        // @ts-ignore
        cy.login();
        // @ts-ignore
        cy.createArticleWithParentComment(1).then(resp => {
            cy.visit(`/article/${resp.article}`);

            cy.get('#root-comments')
                .get('app-single-comment')
                .first()
                .get('.comment')
                .get('.buttons')
                .get('.like-count')
                .should('contain.text', '0'); // Ensure that comment has no likes

            cy.get('#root-comments')
                .get('app-single-comment')
                .first()
                .get('.comment')
                .get('.buttons')
                .get('.like-count')
                .click(); // Like the comment

            cy.get('#root-comments')
                .get('app-single-comment')
                .first()
                .get('.comment')
                .get('.buttons')
                .get('.like-count')
                .should('contain.text', '1'); // Ensure that comment has has one like, ours
        });
    });
});
