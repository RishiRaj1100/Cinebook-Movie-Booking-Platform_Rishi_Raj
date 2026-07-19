import React, { useState, useEffect } from 'react';
import { X, ShieldCheck, CreditCard, AlertCircle } from 'lucide-react';
import { Show, ShowSeat, Booking } from '../types';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';

interface SeatMapModalProps {
  show: Show;
  onClose: () => void;
  onSuccess: (booking: Booking) => void;
  onOpenAuth: () => void;
}

export const SeatMapModal: React.FC<SeatMapModalProps> = ({ show, onClose, onSuccess, onOpenAuth }) => {
  const { user } = useAuth();
  const [seats, setSeats] = useState<ShowSeat[]>([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [holding, setHolding] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchSeatMap();
  }, [show.id]);

  const fetchSeatMap = async () => {
    try {
      setLoading(true);
      const data = await api.getSeatMap(show.id);
      setSeats(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load seat map');
    } finally {
      setLoading(false);
    }
  };

  const toggleSeatSelection = (seat: ShowSeat) => {
    if (seat.status !== 'AVAILABLE') return;

    if (selectedSeatIds.includes(seat.id)) {
      setSelectedSeatIds(selectedSeatIds.filter((id) => id !== seat.id));
    } else {
      if (selectedSeatIds.length >= 6) {
        setError('Maximum 6 seats allowed per transaction');
        return;
      }
      setError('');
      setSelectedSeatIds([...selectedSeatIds, seat.id]);
    }
  };

  const selectedSeats = seats.filter((s) => selectedSeatIds.includes(s.id));
  const subtotalPaise = selectedSeats.reduce((sum, s) => sum + s.price, 0);
  const convenienceFeePaise = Math.round(subtotalPaise * 0.025);
  const gstPaise = Math.round(subtotalPaise * 0.18);
  const totalAmountPaise = subtotalPaise + convenienceFeePaise + gstPaise;

  const handleCheckout = async () => {
    if (!user) {
      onOpenAuth();
      return;
    }

    if (selectedSeatIds.length === 0) return;

    try {
      setHolding(true);
      setError('');

      // Step 1: Hold seats (PESSIMISTIC_WRITE lock concurrency check)
      await api.holdSeats(show.id, selectedSeatIds);

      // Step 2: Create Booking record
      const booking = await api.createBooking(show.id, selectedSeatIds);

      // Step 3: Initiate Razorpay Order
      const order = await api.createPaymentOrder(booking.id);

      // Step 4: Launch Razorpay Checkout Popup
      const options = {
        key: order.razorpayKeyId,
        amount: order.amount,
        currency: order.currency,
        name: 'CineBook Tickets',
        description: `Booking for ${show.movie.title}`,
        order_id: order.orderId,
        handler: async function (response: any) {
          try {
            await api.verifyPayment({
              bookingId: booking.id,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            onSuccess(booking);
            onClose();
          } catch (err: any) {
            setError(err.message || 'Payment verification failed');
          }
        },
        prefill: {
          email: user.email,
          name: user.fullName || '',
        },
        theme: {
          color: '#e50914',
        },
      };

      if ((window as any).Razorpay) {
        const rzp = new (window as any).Razorpay(options);
        rzp.open();
      } else {
        // Fallback for demo without key: directly verify
        await api.verifyPayment({
          bookingId: booking.id,
          razorpayOrderId: order.orderId,
          razorpayPaymentId: 'pay_demo_' + Date.now(),
          razorpaySignature: 'demo_sig',
        });
        onSuccess(booking);
        onClose();
      }
    } catch (err: any) {
      setError(err.message || 'Seat lock or checkout failed. Another user may have picked these seats.');
      fetchSeatMap(); // Refresh seat map
    } finally {
      setHolding(false);
    }
  };

  // Group seats by row
  const rowsMap: Record<string, ShowSeat[]> = {};
  seats.forEach((s) => {
    const r = s.seat.rowLabel;
    if (!rowsMap[r]) rowsMap[r] = [];
    rowsMap[r].push(s);
  });

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" style={{ maxWidth: 720 }} onClick={(e) => e.stopPropagation()}>
        <button
          onClick={onClose}
          style={{ position: 'absolute', top: 16, right: 16, background: 'none', border: 'none', color: '#9ca3af', cursor: 'pointer' }}
        >
          <X size={20} />
        </button>

        <h2 style={{ fontSize: '1.25rem', fontWeight: 800, marginBottom: '0.25rem' }}>{show.movie.title}</h2>
        <p style={{ color: '#9ca3af', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
          {show.screen.theater?.name} • {show.screen.name} • {new Date(show.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
        </p>

        {error && (
          <div style={{ background: 'rgba(229,9,20,0.15)', border: '1px solid #e50914', color: '#f87171', padding: '10px 14px', borderRadius: 8, fontSize: '0.85rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: 8 }}>
            <AlertCircle size={16} /> {error}
          </div>
        )}

        {/* Cinema Screen Curved Bar */}
        <div className="screen-display">
          <span className="screen-text">Screen This Way</span>
        </div>

        {/* Seat Grid */}
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#9ca3af' }}>Loading interactive seat map...</div>
        ) : (
          <div className="seat-grid">
            {Object.keys(rowsMap).sort().map((rowLabel) => (
              <div key={rowLabel} className="seat-row">
                <div className="row-label">{rowLabel}</div>
                {rowsMap[rowLabel].map((seat) => {
                  const isSelected = selectedSeatIds.includes(seat.id);
                  let className = 'seat ';
                  if (seat.status === 'BOOKED') className += 'seat-booked';
                  else if (seat.status === 'LOCKED') className += 'seat-locked';
                  else if (isSelected) className += 'seat-selected';
                  else className += 'seat-available';

                  return (
                    <div
                      key={seat.id}
                      className={className}
                      onClick={() => toggleSeatSelection(seat)}
                      title={`${seat.seat.rowLabel}${seat.seat.seatNumber} — ₹${(seat.price / 100).toFixed(0)} (${seat.seat.seatType})`}
                    >
                      {seat.seat.seatNumber}
                    </div>
                  );
                })}
              </div>
            ))}
          </div>
        )}

        {/* Seat Legend */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: '1.5rem', fontSize: '0.8rem', color: '#9ca3af', marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <div className="seat seat-available" style={{ width: 18, height: 18 }}></div> Available
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <div className="seat seat-selected" style={{ width: 18, height: 18 }}></div> Selected
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <div className="seat seat-locked" style={{ width: 18, height: 18 }}></div> Held
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <div className="seat seat-booked" style={{ width: 18, height: 18 }}></div> Booked
          </div>
        </div>

        {/* Summary Footer */}
        <div className="glass-panel" style={{ padding: '1rem 1.25rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ fontSize: '0.85rem', color: '#9ca3af' }}>
              {selectedSeatIds.length} seat(s) selected
            </div>
            <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#f3f4f6' }}>
              ₹{(totalAmountPaise / 100).toFixed(2)}
            </div>
          </div>

          <button
            className="btn btn-primary"
            disabled={selectedSeatIds.length === 0 || holding}
            onClick={handleCheckout}
          >
            <CreditCard size={18} /> {holding ? 'Holding Seats...' : 'Proceed to Pay'}
          </button>
        </div>
      </div>
    </div>
  );
};
