import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { Layout } from './components/layout/Layout';
import { Home } from './pages/Home';
import { MyBookings } from './pages/MyBookings';
import { AllMovies } from './pages/AllMovies';
import { ScrollToTop } from './components/layout/ScrollToTop';

function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <Toaster position="top-right" />
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="bookings" element={<MyBookings />} />
          <Route path="movies" element={<AllMovies />} />
          {/* Add more routes here like /movie/:id, /booking/:showId */}
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
