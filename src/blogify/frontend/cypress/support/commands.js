// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

Cypress.Commands.add('login', asAdmin => {
    const admin = asAdmin ? '?asAdmin=1': '';
    cy.request('POST', `/test/seed/auth/login/${admin}`).then(resp => {
        const userToken = resp.body.token;
        console.log(userToken);
        window.localStorage.setItem('keepLoggedIn', 'true');
        window.localStorage.setItem('userToken', userToken);
    });
});

Cypress.Commands.add('signup', () => {
    cy.request('POST', '/test/seed/auth/signup').then(resp => {
        return resp.body;
    });
});

Cypress.Commands.add('resetArticles', () => {
    cy.request('DELETE', '/test/seed/article/').then(resp => {
        return resp.body;
    });
});
