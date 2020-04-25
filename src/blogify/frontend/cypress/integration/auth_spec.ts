describe('Authentication Test', () => {
    beforeEach(() => {
        cy.viewport(1920, 1080)
    })

    it('should log the user in', () => {
        const { username, password } = { username: 'user', password: 'pass' }

        cy.visit('login?redirect=%2Fhome')

        cy.get('#login-input-username').type(username)

        cy.get('#login-input-password').type(password)

        cy.get('button').contains('Login').click()

        cy.get('#profile')
            .get('.user-display')
            .get('.display-info')
            .get('.primary-info')
            .should('contain.text', username)
    })

    it('should register a new user', () => {
        cy.visit('login?redirect=%2Fhome')

        cy.get('h2').contains('Register').click()

        const username = Math.random().toString(36).substring(7)

        cy.get('#register-input-username').type(username)

        cy.get('#register-input-password').type('password')

        cy.get('#register-input-name').type('the name')

        cy.get('#register-input-email').type('email@bruh.com')

        cy.get('button').contains('Register').click()

        cy.get('#profile')
            .get('.user-display')
            .get('.display-info')
            .get('.primary-info')
            .should('contain.text', username)
    })
})
