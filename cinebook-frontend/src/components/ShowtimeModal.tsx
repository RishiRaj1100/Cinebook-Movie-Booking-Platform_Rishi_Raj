import React, { useState, useEffect } from 'react';
import { X, Clock, MapPin, Ticket, Calendar, Navigation, Building2, Play } from 'lucide-react';
import { Movie, Show } from '../types';
import { api } from '../services/api';

interface ShowtimeModalProps {
  movie: Movie;
  onClose: () => void;
  onSelectShow: (show: Show) => void;
  onPlayTrailer?: (movie: Movie) => void;
}

export const ShowtimeModal: React.FC<ShowtimeModalProps> = ({ movie, onClose, onSelectShow, onPlayTrailer }) => {
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [selectedCity, setSelectedCity] = useState<string>('All');
  const [allCities, setAllCities] = useState<string[]>([]);
  const [shows, setShows] = useState<Show[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchShows();
  }, [movie.id, selectedDate]);

  useEffect(() => {
    api.getCities().then(setAllCities).catch(() => setAllCities([]));
  }, []);

  const fetchShows = async () => {
    setLoading(true);
    try {
      const data = await api.getShows(movie.id, selectedDate);
      setShows(data);
    } catch (err: any) {
      setShows([]);
    } finally {
      setLoading(false);
    }
  };

  // Generate next 5 dates with full formatted labels
  const dates = Array.from({ length: 5 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() + i);
    const dateStr = d.toISOString().split('T')[0];
    return {
      dateStr,
      dayName: i === 0 ? 'Today' : d.toLocaleDateString('en-US', { weekday: 'short' }),
      dayNum: d.getDate(),
      month: d.toLocaleDateString('en-US', { month: 'short' }),
      fullFormatted: d.toLocaleDateString('en-US', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }),
    };
  });

  const activeDateObj = dates.find(d => d.dateStr === selectedDate) || dates[0];

  // Group shows by theater
  const filteredShows = shows.filter(show => {
    if (selectedCity === 'All') return true;
    const city = show.screen?.theater?.city || '';
    return city.toLowerCase() === selectedCity.toLowerCase();
  });

  const theaterMap = new Map<string, { theater: any; shows: Show[] }>();
  filteredShows.forEach(show => {
    const t = show.screen?.theater;
    const tKey = t ? t.id : 'default';
    if (!theaterMap.has(tKey)) {
      theaterMap.set(tKey, { theater: t, shows: [] });
    }
    theaterMap.get(tKey)!.shows.push(show);
  });

  // Combine fetched cities with cities present in current shows
  const availableCities = Array.from(
    new Set([...allCities, ...shows.map(s => s.screen?.theater?.city).filter(Boolean)])
  );

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: 760, padding: '2rem' }} onClick={(e) => e.stopPropagation()}>
        <button
          onClick={onClose}
          style={{ position: 'absolute', top: 16, right: 16, background: 'none', border: 'none', color: '#9ca3af', cursor: 'pointer' }}
        >
          <X size={20} />
        </button>

        {/* Movie Header */}
        <div style={{ display: 'flex', gap: 16, marginBottom: '1.5rem', alignItems: 'center' }}>
          <img
            src={movie.posterUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&q=80'}
            alt={movie.title}
            style={{ width: 75, height: 105, borderRadius: 12, objectFit: 'cover', boxShadow: '0 4px 12px rgba(0,0,0,0.5)' }}
          />
          <div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#f3f4f6', marginBottom: 4 }}>{movie.title}</h2>
            <div style={{ fontSize: '0.85rem', color: '#9ca3af', display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              <span>{movie.genre}</span>
              <span>•</span>
              <span>{movie.durationMinutes} mins</span>
              <span>•</span>
              <span style={{ color: '#e50914', fontWeight: 600 }}>{movie.language}</span>
            </div>
            <div style={{ fontSize: '0.8rem', color: '#6b7280', marginTop: 6, display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                <Calendar size={14} color="#e50914" /> {activeDateObj.fullFormatted}
              </span>
              {onPlayTrailer && (
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => onPlayTrailer(movie)}
                  style={{ padding: '4px 10px', fontSize: '0.75rem', borderRadius: 12, display: 'flex', alignItems: 'center', gap: 4 }}
                >
                  <Play size={12} fill="#ffffff" /> Watch Trailer
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Date & City Selector Control Bar */}
        <div style={{ background: 'rgba(255,255,255,0.03)', padding: '1rem', borderRadius: 14, border: '1px solid rgba(255,255,255,0.08)', marginBottom: '1.25rem', display: 'flex', flexDirection: 'column', gap: 12 }}>
          {/* Header Bar: City Selection Label */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#e50914', textTransform: 'uppercase', letterSpacing: 0.5, display: 'flex', alignItems: 'center', gap: 6 }}>
              <Building2 size={16} /> Select Location / City:
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <MapPin size={16} color="#e50914" />
              <select
                className="form-input"
                style={{ width: 'auto', padding: '6px 14px', fontSize: '0.875rem', fontWeight: 600, background: '#111827', borderColor: 'rgba(229,9,20,0.5)', color: '#ffffff' }}
                value={selectedCity}
                onChange={(e) => setSelectedCity(e.target.value)}
              >
                <option value="All">All Cities ({availableCities.length > 0 ? availableCities.length : 'All'})</option>
                {availableCities.map((city) => (
                  <option key={city} value={city}>{city}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Dates Bar */}
          <div style={{ display: 'flex', gap: 8, overflowX: 'auto', paddingTop: 4 }}>
            {dates.map((d) => (
              <button
                key={d.dateStr}
                onClick={() => setSelectedDate(d.dateStr)}
                style={{
                  flex: 1,
                  minWidth: 90,
                  padding: '8px 12px',
                  textAlign: 'center',
                  cursor: 'pointer',
                  border: selectedDate === d.dateStr ? '2px solid #e50914' : '1px solid rgba(255,255,255,0.1)',
                  background: selectedDate === d.dateStr ? 'rgba(229, 9, 20, 0.2)' : 'rgba(255,255,255,0.03)',
                  borderRadius: 10,
                  transition: 'all 0.2s ease',
                }}
              >
                <div style={{ fontSize: '0.7rem', color: selectedDate === d.dateStr ? '#e50914' : '#9ca3af', textTransform: 'uppercase', fontWeight: 700 }}>{d.dayName}</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, color: selectedDate === d.dateStr ? '#e50914' : '#f3f4f6' }}>{d.dayNum}</div>
                <div style={{ fontSize: '0.7rem', color: '#9ca3af' }}>{d.month}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Shows grouped by Theater */}
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#9ca3af' }}>Loading theater showtimes & locations...</div>
        ) : theaterMap.size === 0 ? (
          <div style={{ padding: '2.5rem', textAlign: 'center', color: '#9ca3af', background: 'rgba(255,255,255,0.02)', borderRadius: 12, border: '1px border var(--border-color)' }}>
            No showtimes scheduled for {activeDateObj.fullFormatted} in {selectedCity === 'All' ? 'any city' : selectedCity}. Try selecting another date!
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', maxHeight: 400, overflowY: 'auto', paddingRight: 4 }}>
            {Array.from(theaterMap.values()).map(({ theater, shows }) => (
              <div key={theater?.id || Math.random()} className="glass-panel" style={{ padding: '1.25rem' }}>
                {/* Theater Name & Full Address */}
                <div style={{ marginBottom: '1rem' }}>
                  <div style={{ fontSize: '1.05rem', fontWeight: 800, color: '#f3f4f6', display: 'flex', alignItems: 'center', gap: 6 }}>
                    <MapPin size={18} color="#e50914" /> {theater?.name || 'PVR Cinemas'}
                    {theater?.city && (
                      <span style={{ fontSize: '0.75rem', background: 'rgba(229,9,20,0.2)', color: '#e50914', padding: '2px 8px', borderRadius: 12, fontWeight: 700 }}>
                        {theater.city}
                      </span>
                    )}
                  </div>
                  {theater?.address && (
                    <div style={{ fontSize: '0.825rem', color: '#9ca3af', marginTop: 4, display: 'flex', alignItems: 'center', gap: 4, paddingLeft: 24 }}>
                      <Navigation size={12} color="#6b7280" /> {theater.address}
                    </div>
                  )}
                </div>

                {/* Showtime Pills */}
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                  {shows.map((show) => {
                    const startTimeFormatted = new Date(show.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                    return (
                      <button
                        key={show.id}
                        className="btn btn-primary"
                        onClick={() => onSelectShow(show)}
                        style={{
                          padding: '8px 16px',
                          fontSize: '0.875rem',
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          gap: 2,
                        }}
                      >
                        <div style={{ display: 'flex', alignItems: 'center', gap: 4, fontWeight: 800 }}>
                          <Clock size={13} /> {startTimeFormatted}
                        </div>
                        <div style={{ fontSize: '0.7rem', opacity: 0.85 }}>
                          {show.screen?.name} • ₹{show.basePrice / 100}
                        </div>
                      </button>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
