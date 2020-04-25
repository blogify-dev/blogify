describe('Authentication Test', () => {
    beforeEach(() => {
        cy.viewport(1920, 1080)
    })
    it('logs the user in', () => {
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
})
