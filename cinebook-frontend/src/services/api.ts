import { AuthResponse, Booking, HoldSeatsResponse, Movie, PaymentOrderResponse, Show, ShowSeat, Theater } from '../types';

const API_BASE = '/api';

function getHeaders(): HeadersInit {
  const token = localStorage.getItem('token');
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers as unknown as HeadersInit;
}

async function handleResponse<T>(res: Response): Promise<T> {
  const text = await res.text();
  let json: any = {};

  if (text && text.trim().length > 0) {
    try {
      json = JSON.parse(text);
    } catch (e) {
      throw new Error(`Server response error (${res.status}): ${text.substring(0, 150)}`);
    }
  }

  if (res.status === 401 || res.status === 403) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    throw new Error(json.message || json.error || 'Authentication required or session expired. Please sign in again.');
  }

  if (!res.ok || (json && json.success === false)) {
    throw new Error(json.message || json.error || `API Request failed with status ${res.status}`);
  }

  return json.data !== undefined ? json.data : json;
}

export const api = {
  // Auth
  async register(data: any): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse<AuthResponse>(res);
  },

  async login(data: any): Promise<AuthResponse> {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
    });
    return handleResponse<AuthResponse>(res);
  },

  // Movies
  async getMovies(genre?: string, language?: string): Promise<Movie[]> {
    const params = new URLSearchParams();
    if (genre) params.append('genre', genre);
    if (language) params.append('language', language);
    const res = await fetch(`${API_BASE}/movies?${params.toString()}`);
    return handleResponse<Movie[]>(res);
  },

  async getMovieById(id: string): Promise<Movie> {
    const res = await fetch(`${API_BASE}/movies/${id}`);
    return handleResponse<Movie>(res);
  },

  async getGenres(): Promise<string[]> {
    const res = await fetch(`${API_BASE}/movies/genres`);
    return handleResponse<string[]>(res);
  },

  async getLanguages(): Promise<string[]> {
    const res = await fetch(`${API_BASE}/movies/languages`);
    return handleResponse<string[]>(res);
  },

  // Theaters & Shows
  async getTheaters(city?: string): Promise<Theater[]> {
    const params = city ? `?city=${encodeURIComponent(city)}` : '';
    const res = await fetch(`${API_BASE}/theaters${params}`);
    return handleResponse<Theater[]>(res);
  },

  async getCities(): Promise<string[]> {
    const res = await fetch(`${API_BASE}/theaters/cities`);
    return handleResponse<string[]>(res);
  },

  async getShows(movieId: string, date: string): Promise<Show[]> {
    const res = await fetch(`${API_BASE}/shows?movieId=${movieId}&date=${date}`);
    return handleResponse<Show[]>(res);
  },

  async getShowById(id: string): Promise<Show> {
    const res = await fetch(`${API_BASE}/shows/${id}`);
    return handleResponse<Show>(res);
  },

  async getSeatMap(showId: string): Promise<ShowSeat[]> {
    const res = await fetch(`${API_BASE}/shows/${showId}/seats`);
    return handleResponse<ShowSeat[]>(res);
  },

  // Booking & Concurrency Seat Lock
  async holdSeats(showId: string, showSeatIds: string[]): Promise<HoldSeatsResponse> {
    const res = await fetch(`${API_BASE}/shows/hold-seats`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ showId, showSeatIds }),
    });
    return handleResponse<HoldSeatsResponse>(res);
  },

  async createBooking(showId: string, showSeatIds: string[]): Promise<Booking> {
    const res = await fetch(`${API_BASE}/bookings`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify({ showId, showSeatIds }),
    });
    return handleResponse<Booking>(res);
  },

  async getUserBookings(): Promise<Booking[]> {
    const res = await fetch(`${API_BASE}/bookings`, {
      headers: getHeaders(),
    });
    return handleResponse<Booking[]>(res);
  },

  async getPublicBooking(bookingId: string): Promise<Booking> {
    const res = await fetch(`${API_BASE}/bookings/public/${bookingId}`);
    return handleResponse<Booking>(res);
  },

  async cancelBooking(bookingId: string): Promise<void> {
    const res = await fetch(`${API_BASE}/bookings/${bookingId}/cancel`, {
      method: 'POST',
      headers: getHeaders(),
    });
    return handleResponse<void>(res);
  },

  // Payments
  async createPaymentOrder(bookingId: string): Promise<PaymentOrderResponse> {
    const res = await fetch(`${API_BASE}/payments/create-order?bookingId=${bookingId}`, {
      method: 'POST',
      headers: getHeaders(),
    });
    return handleResponse<PaymentOrderResponse>(res);
  },

  async verifyPayment(data: {
    bookingId: string;
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
  }): Promise<boolean> {
    const res = await fetch(`${API_BASE}/payments/verify`, {
      method: 'POST',
      headers: getHeaders(),
      body: JSON.stringify(data),
    });
    return handleResponse<boolean>(res);
  },

  // Spring AI Recommendations
  async getAiRecommendations(query: string): Promise<string> {
    const res = await fetch(`${API_BASE}/ai/recommend?query=${encodeURIComponent(query)}`, {
      method: 'POST',
      headers: getHeaders(),
    });
    return handleResponse<string>(res);
  },

  // TMDB Import (Admin)
  async importTmdbMovie(tmdbId: number): Promise<Movie> {
    const res = await fetch(`${API_BASE}/tmdb/import/${tmdbId}`, {
      method: 'POST',
      headers: getHeaders(),
    });
    return handleResponse<Movie>(res);
  },
};
