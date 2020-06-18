describe('Articles Test', () => {
    beforeEach(() => {
        cy.viewport(1920, 1080);
    });

    it('5 articles should be shown properly', () => {
        cy.request('POST', '/test/seed/article?amount=5');
        cy.visit('/');
        cy.get('#articles-main').each(element => {
            cy.wrap(element).get('app-single-article-box').get('h1').should('contain.text', 'The title of article');
        });
    });


    it('Should create article and navigate', () => {
        // @ts-ignore
        cy.login();

        const title = 'title';
        const content = 'content';
        const summary = 'summary';
        const categories = ['cat 1', 'cat 2'];

        cy.visit('/article/new');
        cy.get('#title-input').type(title);
        cy.get('#summary-input').type(summary);
        cy.get('#content-row').get('textarea').type(content);

        categories.forEach(cat => {
            cy.get('#new-category-input').type(cat).type('{enter}');
        });

        cy.get('#submit_button').click();

        cy.url().should('match', /\/article\/[a-z0-9-]+/g);
    });

    it('Should show article properly', () => {
        cy.request('POST', '/test/seed/article/?amount=1').then(resp => {
            const uuid = resp.body.created[0];
            cy.visit(`/article/${uuid}`);
            cy.get('#title').should('contain.text', 'The title of article');
            cy.get('app-show-article .container-article .article-header .summary-row').should('contain.text', 'Article\'s summary, short little summary');
            cy.get('#article-content').should('contain.text', 'Content of content. should be a lot of text');

            cy.get('#user')
                .get('.user-display')
                .get('.display-info')
                .get('.primary-info')
                .should('contain.text', 'test_user');

            cy.get('#icons-left')
                .get('.likes')
                .should('not.have.class', 'clickable');
            
        });
    });

    it('Should delete article and navigate', () => {
        // @ts-ignore
        cy.login();
        cy.request('POST', '/test/seed/article/?amount=1').then(resp => {
            const uuid = resp.body.created[0];
            cy.visit(`/article/${uuid}`);
            cy.get('#button-delete').click();

            cy.url().should('match', /\/home/g);
        });
    });

    it('Should edit article and navigate', () => {
        // @ts-ignore
        cy.login();
        cy.request('POST', '/test/seed/article/?amount=1').then(resp => {
            const uuid = resp.body.created[0];
            cy.visit(`/article/${uuid}`);
            cy.get('#button-update').click();

            cy.url().should('match', /\/article\/update\/[a-z0-9-]+/g);

            const title = 'title';
            const content = 'content';
            const summary = 'summary';
            const categories = ['cat 1', 'cat 2'];

            cy.get('#title-input').type(title);
            cy.get('#summary-input').type(summary);
            cy.get('#content-row').get('textarea').type(content);

            categories.forEach(cat => {
                cy.get('#new-category-input').type(cat).type('{enter}');
            });

            cy.get('button').contains('Submit Changes').click();

            cy.url().should('match', /\/article\/[a-z0-9-]+/g);
        });
    });
});
