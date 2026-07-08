import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { Layout } from './components/layout/Layout';
import { Home } from './pages/Home';
import { MyBookings } from './pages/MyBookings';
import { AllMovies } from './pages/AllMovies';
import { MovieDetails } from './pages/MovieDetails';
import { BookingPage } from './pages/BookingPage';
import { PaymentPage } from './pages/PaymentPage';
import { ScrollToTop } from './components/layout/ScrollToTop';

import { AgentChat } from './components/agent/AgentChat';

function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <Toaster position="top-right" />
      <AgentChat />
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="bookings" element={<MyBookings />} />
          <Route path="movies" element={<AllMovies />} />
          <Route path="movies/:id" element={<MovieDetails />} />
          <Route path="book/:showId" element={<BookingPage />} />
          <Route path="payment/:bookingId" element={<PaymentPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
