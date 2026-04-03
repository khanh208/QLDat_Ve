const http = require('http');
const { URL } = require('url');

const port = Number(process.env.MOCK_API_PORT || 8081);
const host = process.env.MOCK_API_HOST || '127.0.0.1';
const frontendBaseUrl = process.env.FRONTEND_BASE_URL || 'http://127.0.0.1:3000';

const trips = [
  {
    tripId: 1,
    departureTime: '2026-04-10T08:00:00',
    arrivalTime: '2026-04-10T14:00:00',
    basePrice: 250000,
    route: {
      routeId: 1,
      startLocation: 'TP. Ho Chi Minh',
      endLocation: 'Da Lat',
      distanceKm: 320,
    },
    vehicle: {
      vehicleId: 1,
      licensePlate: '51B-99999',
      vehicleType: 'Limousine',
      totalSeats: 8,
    },
  },
  {
    tripId: 2,
    departureTime: '2026-04-10T20:30:00',
    arrivalTime: '2026-04-11T02:30:00',
    basePrice: 180000,
    route: {
      routeId: 2,
      startLocation: 'TP. Ho Chi Minh',
      endLocation: 'Nha Trang',
      distanceKm: 430,
    },
    vehicle: {
      vehicleId: 2,
      licensePlate: '79B-12345',
      vehicleType: 'Giuong nam',
      totalSeats: 8,
    },
  },
];

const bookedSeatsByTrip = new Map([
  [1, new Set(['A1', 'B2'])],
  [2, new Set(['D1'])],
]);

let bookingIdCounter = 900;

const feedbackByTrip = new Map([
  [1, [
    {
      feedbackId: 1,
      rating: 5,
      comment: 'Xe sach va dung gio.',
      feedbackTime: '2026-04-01T09:00:00',
      user: {
        username: 'mock-user',
      },
    },
  ]],
  [2, []],
]);

const users = [
  {
    userId: 1,
    username: 'selenium-user',
    password: '123456',
    role: 'USER',
    email: 'selenium@example.com',
  },
];

function findTrip(tripId) {
  return trips.find((trip) => trip.tripId === tripId);
}

function buildBooking(bookingId, tripId, status, seatNumbers, paymentMethod, extra = {}) {
  const trip = findTrip(tripId);
  return {
    bookingId,
    status,
    paymentMethod,
    bookingTime: extra.bookingTime || new Date().toISOString(),
    totalAmount: Number(trip.basePrice) * seatNumbers.length,
    momoOrderId: extra.momoOrderId || null,
    momoRequestId: extra.momoRequestId || null,
    trip,
    bookingDetails: seatNumbers.map((seatNumber) => ({ seatNumber })),
  };
}

const bookings = [
  buildBooking(701, 1, 'CONFIRMED', ['B2'], 'CASH', {
    bookingTime: '2026-04-02T08:30:00',
  }),
  buildBooking(702, 2, 'PENDING', ['D1'], 'MOMO', {
    bookingTime: '2026-04-02T11:15:00',
    momoOrderId: 'ORD-702',
    momoRequestId: 'REQ-702',
  }),
];

function sendJson(res, statusCode, payload) {
  const body = JSON.stringify(payload);
  res.writeHead(statusCode, {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, OPTIONS',
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
  });
  res.end(body);
}

function sendNotFound(res, message = 'Mock resource not found.') {
  sendJson(res, 404, { message });
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let raw = '';
    req.on('data', (chunk) => {
      raw += chunk;
    });
    req.on('end', () => {
      if (!raw) {
        resolve({});
        return;
      }
      try {
        resolve(JSON.parse(raw));
      } catch (error) {
        reject(error);
      }
    });
    req.on('error', reject);
  });
}

function matchTrip(pathname) {
  const tripMatch = pathname.match(/^\/api\/trips\/(\d+)$/);
  const bookedSeatsMatch = pathname.match(/^\/api\/trips\/(\d+)\/booked-seats$/);
  const feedbackMatch = pathname.match(/^\/api\/trips\/(\d+)\/feedback$/);

  if (tripMatch) {
    return { type: 'trip', tripId: Number(tripMatch[1]) };
  }
  if (bookedSeatsMatch) {
    return { type: 'bookedSeats', tripId: Number(bookedSeatsMatch[1]) };
  }
  if (feedbackMatch) {
    return { type: 'feedback', tripId: Number(feedbackMatch[1]) };
  }
  return null;
}

function addSeatsToHolding(tripId, seatNumbers) {
  const bookedSeats = bookedSeatsByTrip.get(tripId) || new Set();
  seatNumbers.forEach((seat) => bookedSeats.add(seat));
  bookedSeatsByTrip.set(tripId, bookedSeats);
}

function removeSeatsFromHolding(tripId, seatNumbers) {
  const bookedSeats = bookedSeatsByTrip.get(tripId) || new Set();
  seatNumbers.forEach((seat) => bookedSeats.delete(seat));
  bookedSeatsByTrip.set(tripId, bookedSeats);
}

function handleTripSearch(url, res) {
  const startLocation = url.searchParams.get('start_location');
  const endLocation = url.searchParams.get('end_location');

  const filteredTrips = trips.filter((trip) => {
    if (startLocation && trip.route.startLocation !== startLocation) {
      return false;
    }
    if (endLocation && trip.route.endLocation !== endLocation) {
      return false;
    }
    return true;
  });

  sendJson(res, 200, filteredTrips);
}

function handleBookingCreate(payload, res) {
  const tripId = Number(payload.tripId);
  const trip = findTrip(tripId);
  if (!trip) {
    sendJson(res, 404, { message: 'Mock trip not found.' });
    return;
  }

  const seatNumbers = Array.isArray(payload.seatNumbers)
    ? payload.seatNumbers.filter((seat) => typeof seat === 'string' && seat.trim() !== '')
    : [];

  if (seatNumbers.length === 0) {
    sendJson(res, 400, { message: 'Please choose at least one seat.' });
    return;
  }

  const bookedSeats = bookedSeatsByTrip.get(tripId) || new Set();
  const duplicateSeat = seatNumbers.find((seat) => bookedSeats.has(seat));
  if (duplicateSeat) {
    sendJson(res, 409, { message: `Seat ${duplicateSeat} is already held.` });
    return;
  }

  const bookingId = ++bookingIdCounter;
  const paymentMethod = String(payload.paymentMethod || '').toUpperCase();
  addSeatsToHolding(tripId, seatNumbers);

  if (paymentMethod === 'CASH') {
    bookings.unshift(buildBooking(bookingId, tripId, 'CONFIRMED', seatNumbers, paymentMethod));
    sendJson(res, 200, {
      bookingId,
      status: 'CONFIRMED',
      message: 'Cash booking mock success.',
    });
    return;
  }

  const momoOrderId = `ORD-${bookingId}`;
  const momoRequestId = `REQ-${bookingId}`;
  bookings.unshift(buildBooking(bookingId, tripId, 'PENDING', seatNumbers, paymentMethod, {
    momoOrderId,
    momoRequestId,
  }));

  sendJson(res, 200, {
    bookingId,
    status: 'PENDING',
    momoOrderId,
    momoRequestId,
    message: 'MoMo booking mock created.',
  });
}

function handleMomoCheckout(pathname, res) {
  const match = pathname.match(/^\/api\/payment\/momo\/checkout\/(\d+)$/);
  if (!match) {
    sendNotFound(res);
    return;
  }

  const bookingId = Number(match[1]);
  const booking = bookings.find((item) => item.bookingId === bookingId);
  if (!booking) {
    sendNotFound(res, 'Mock booking not found.');
    return;
  }

  sendJson(res, 200, {
    payUrl: `${frontendBaseUrl}/payment-success?orderId=${booking.momoOrderId || `ORD-${bookingId}`}&requestId=${booking.momoRequestId || `REQ-${bookingId}`}&resultCode=0&message=Success&amount=${booking.totalAmount}&transId=TRANS-${bookingId}`,
  });
}

function handleMomoConfirm(payload, res) {
  const orderId = payload.orderId || '';
  const requestId = payload.requestId || '';
  const booking = bookings.find((item) =>
    (item.momoOrderId === orderId && item.momoRequestId === requestId) ||
    item.bookingId === Number(String(orderId).replace('ORD-', ''))
  );

  if (booking) {
    booking.status = 'CONFIRMED';
  }

  sendJson(res, 200, {
    status: 'success',
    message: 'MoMo confirmation mock completed.',
  });
}

function handleMyBookings(req, res) {
  if (!req.headers.authorization) {
    sendJson(res, 401, { message: 'Unauthorized mock request.' });
    return;
  }

  sendJson(res, 200, bookings);
}

function handleCancelBooking(pathname, res) {
  const match = pathname.match(/^\/api\/bookings\/(\d+)\/cancel$/);
  if (!match) {
    sendNotFound(res);
    return;
  }

  const bookingId = Number(match[1]);
  const booking = bookings.find((item) => item.bookingId === bookingId);
  if (!booking) {
    sendNotFound(res, 'Mock booking not found.');
    return;
  }

  booking.status = 'CANCELLED';
  removeSeatsFromHolding(booking.trip.tripId, booking.bookingDetails.map((detail) => detail.seatNumber));
  sendJson(res, 200, booking);
}

function handleLogin(payload, res) {
  const username = payload.username || '';
  const password = payload.password || '';
  const user = users.find((item) => item.username === username && item.password === password);

  if (!user) {
    sendJson(res, 401, { message: 'Invalid username or password.' });
    return;
  }

  sendJson(res, 200, {
    token: 'mock-token',
    userId: user.userId,
    role: user.role,
    username: user.username,
  });
}

const server = http.createServer(async (req, res) => {
  if (!req.url) {
    sendNotFound(res);
    return;
  }

  const url = new URL(req.url, `http://${host}:${port}`);
  const { pathname } = url;

  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, OPTIONS',
    });
    res.end();
    return;
  }

  if (req.method === 'GET' && pathname === '/api/trips') {
    sendJson(res, 200, trips);
    return;
  }

  if (req.method === 'GET' && pathname === '/api/trips/search') {
    handleTripSearch(url, res);
    return;
  }

  if (req.method === 'GET' && pathname === '/api/bookings/my-bookings') {
    handleMyBookings(req, res);
    return;
  }

  const tripRoute = matchTrip(pathname);
  if (req.method === 'GET' && tripRoute) {
    if (tripRoute.type === 'trip') {
      const trip = findTrip(tripRoute.tripId);
      if (!trip) {
        sendNotFound(res, 'Mock trip not found.');
        return;
      }
      sendJson(res, 200, trip);
      return;
    }

    if (tripRoute.type === 'feedback') {
      sendJson(res, 200, feedbackByTrip.get(tripRoute.tripId) || []);
      return;
    }

    const bookedSeats = [...(bookedSeatsByTrip.get(tripRoute.tripId) || new Set())];
    sendJson(res, 200, bookedSeats);
    return;
  }

  if (req.method === 'POST' && pathname === '/api/bookings') {
    try {
      const payload = await readBody(req);
      handleBookingCreate(payload, res);
    } catch (error) {
      sendJson(res, 400, { message: 'Invalid JSON body.', error: error.message });
    }
    return;
  }

  if (req.method === 'POST' && pathname === '/api/auth/login') {
    try {
      const payload = await readBody(req);
      handleLogin(payload, res);
    } catch (error) {
      sendJson(res, 400, { message: 'Invalid login body.', error: error.message });
    }
    return;
  }

  if (req.method === 'PUT' && pathname.startsWith('/api/bookings/')) {
    handleCancelBooking(pathname, res);
    return;
  }

  if (req.method === 'POST' && pathname === '/api/feedback') {
    try {
      const payload = await readBody(req);
      const feedbackList = feedbackByTrip.get(Number(payload.tripId)) || [];
      const feedback = {
        feedbackId: feedbackList.length + 1,
        rating: Number(payload.rating || 0),
        comment: payload.comment || '',
        feedbackTime: new Date().toISOString(),
        user: {
          username: 'mock-user',
        },
      };
      feedbackList.unshift(feedback);
      feedbackByTrip.set(Number(payload.tripId), feedbackList);
      sendJson(res, 200, feedback);
    } catch (error) {
      sendJson(res, 400, { message: 'Unable to create mock feedback.', error: error.message });
    }
    return;
  }

  if (req.method === 'POST' && pathname.startsWith('/api/payment/momo/checkout/')) {
    handleMomoCheckout(pathname, res);
    return;
  }

  if (req.method === 'POST' && pathname === '/api/payment/momo/confirm') {
    try {
      const payload = await readBody(req);
      handleMomoConfirm(payload, res);
    } catch (error) {
      sendJson(res, 400, { message: 'Invalid MoMo confirm body.', error: error.message });
    }
    return;
  }

  sendNotFound(res);
});

server.listen(port, host, () => {
  console.log(`Mock API server is listening on http://${host}:${port}`);
});
