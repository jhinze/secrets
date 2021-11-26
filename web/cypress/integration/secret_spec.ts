describe('Secrets', () => {

  beforeEach(() => {
    cy.visit('/');
    cy.intercept(
      {
        method: 'POST',
        url: '/api/secret?recaptcha=*'
      },
      {
        "secret": {
          "id": "123-abc",
          "secret": "text"
        },
        "url": "http://localhost:3000/abc-123"
      }
    ).as('postSecret')
    cy.intercept(
      {
        method: 'GET',
        url: '/api/secret/123-abc'
      },
      {
        "id": "123-abc",
        "secret": "get text"
      }
    ).as('getSecret')
  });

  it('creates secret', () => {
    cy.get('[id=secret-text-field]').type('text');
    cy.get('[id=submit-button]').click();
    cy.get('[id=new-button]').should('be.visible');
    cy.get('[id=secret-text-field]').should('be.disabled').and('have.value', 'text');
    cy.get('[id=secret-url]').should('be.visible').and('have.text', 'http://localhost:3000/123-abc');
  });

  it('gets secret', () => {
    cy.visit('/123-abc');
    cy.get('h6').should('have.text', 'Secret 123-abc delivered')
    cy.get('[id=new-button]').should('be.visible');
    cy.get('[id=copy-button]').should('be.visible');
    cy.get('[id=secret-text-field]').should('be.disabled').and('have.value', 'get text');
  });

  it('creates new secret after getting secret', () => {
    cy.visit('/123-abc');
    cy.get('[id=new-button]').click();
    cy.get('[id=secret-text-field]').should('be.enabled').and('have.value', '').type('text');
    cy.get('[id=submit-button]').click();
    cy.get('[id=new-button]').should('be.visible');
    cy.get('[id=secret-text-field]').should('be.disabled').and('have.value', 'text');
    cy.get('[id=secret-url]').should('be.visible').and('have.text', 'http://localhost:3000/123-abc');
  })

  it('fails to save secret', () => {
    cy.intercept(
      {
        method: 'POST',
        url: '/api/secret?recaptcha=*'
      },
      {
        statusCode: 500,
        body: 'error'
      }
    ).as('postSecretFails')
    cy.get('[id=secret-text-field]').type('text');
    cy.get('[id=submit-button]').click();
    cy.get('[id=save-error-alert]').should('be.visible');
    cy.get('[id=secret-text-field]').should('not.be.disabled')
  });

  it('can not find secret', () => {
    cy.intercept(
      {
        method: 'GET',
        url: '/api/secret/not-found'
      },
      {
        statusCode: 404,
        body: 'Not Found'
      }
    ).as('getSecretNotFound')
    cy.visit('/not-found');
    cy.get('[id=not-found-alert]').should('be.visible');
    cy.get('[id=secret-text-field]').should('be.enabled').and('have.value', '');
    cy.get('[id=submit-button]').should('be.visible');
  });

})