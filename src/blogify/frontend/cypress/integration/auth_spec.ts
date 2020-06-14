describe('Authentication Test', () => {
    beforeEach(() => {
        cy.viewport(1920, 1080);
    });

    let registeredUser: string;
    let registeredPass: string;

    it('should register a new user', () => {
        cy.visit('login?redirect=%2Fhome');

        cy.get('h2').contains('Register').click();

        registeredUser = Math.random().toString(36).substring(7);
        registeredPass = Math.random().toString(36).substring(7);

        cy.get('#register-input-username').type(registeredUser);

        cy.get('#register-input-password').type(registeredPass);

        cy.get('#register-input-name').type('the name');

        cy.get('#register-input-email').type('email@bruh.com');

        cy.get('button').contains('Register').click();

        cy.get('#profile')
            .get('.user-display')
            .get('.display-info')
            .get('.primary-info')
            .should('contain.text', registeredUser);
    });

    it('should log the user in', () => {
        cy.visit('login?redirect=%2Fhome');
        // @ts-ignore // Custom cypress command
        cy.signup(); //  This ensures that the user exists by creating it

        cy.get('#login-input-username').type('test_user');

        cy.get('#login-input-password').type('test_pass');

        cy.get('button').contains('Login').click();

        cy.get('#profile')
            .get('.user-display')
            .get('.display-info')
            .get('.primary-info')
            .should('contain.text', 'test_user');
    });

});
