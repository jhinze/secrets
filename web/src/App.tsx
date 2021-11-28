import React from 'react';
import './App.css';
import {
  BrowserRouter,
  Routes,
  Route
} from "react-router-dom";
import {CssBaseline, ThemeProvider} from "@mui/material";
import { createTheme } from '@mui/material/styles'
import Secret from "./Secret";

function App() {

  const theme = createTheme({
    palette: {
      mode: "dark"
    }
  });

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <div className="App" style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
      }}>
        <BrowserRouter basename={process.env.PUBLIC_URL}>
          <Routes>
            <Route path="/:secretId" element={<Secret />}/>
            <Route path="/" element={<Secret />}/>
          </Routes>
        </BrowserRouter>
      </div>
    </ThemeProvider>
  );
}

export default App;
