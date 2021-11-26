import React from 'react';
import { render, screen } from '@testing-library/react';
import App from './App';

test('renders secret', () => {
  render(<App />);
  const linkElement = screen.getByText(/Secret is saved for 24 hours. Secret is only viewable one time./i);
  expect(linkElement).toBeInTheDocument();
});
