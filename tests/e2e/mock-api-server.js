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
      startLocation: 'TP. Hồ Chí Minh',
      endLocation: 'Đà Lạt',
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
      startLocation: 'TP. Hồ Chí Minh',
      endLocation: 'Nha Trang',
      distanceKm: 430,
    },
    vehicle: {
      vehicleId: 2,
      licensePlate: '79B-12345',
      vehicleType: 'Giường nằm',
      totalSeats: 8,
    },
  },
];

const bookedSeatsByTrip = new Map([
  [1, new Set(['A1'])],
  [2, new Set(['D1'])],
]);

let bookingIdCounter = 900;

const feedbackByTrip = new Map([
  [1, [
    {
      feedbackId: 1,
      rating: 5,
      comment: 'Xe sạch và đúng giờ.',
      feedbackTime: '2026-04-01T09:00:00',
      user: {
        username: 'mock-user',
      },
    },
  ]],
  [2, []],
]);

function sendJson(res, statusCode, payload) {
  const body = JSON.stringify(payload);
  res.writeHead(statusCode, {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization, Accept',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
  });
  res.end(body);
}

function sendNotFound(res, message = 'Không tìm thấy dữ liệu mock.') {
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

function findTrip(tripId) {
  return trips.find((trip) => trip.tripId === tripId);
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
    sendJson(res, 404, { message: 'Không tìm thấy chuyến đi mock.' });
    return;
  }

  const seatNumbers = Array.isArray(payload.seatNumbers) ? payload.seatNumbers.filter(Boolean) : [];
  if (seatNumbers.length === 0) {
    sendJson(res, 400, { message: 'Vui lòng chọn ít nhất một ghế.' });
    return;
  }

  const bookedSeats = bookedSeatsByTrip.get(tripId) || new Set();
  const duplicateSeat = seatNumbers.find((seat) => bookedSeats.has(seat));
  if (duplicateSeat) {
    sendJson(res, 409, { message: `Ghế ${duplicateSeat} đã được giữ hoặc đặt trước đó.` });
    return;
  }

  const bookingId = ++bookingIdCounter;
  const paymentMethod = String(payload.paymentMethod || '').toUpperCase();

  if (paymentMethod === 'CASH') {
    seatNumbers.forEach((seat) => bookedSeats.add(seat));
    bookedSeatsByTrip.set(tripId, bookedSeats);
    sendJson(res, 200, {
      bookingId,
      status: 'CONFIRMED',
      message: 'Đặt vé tiền mặt thành công (mock).',
    });
    return;
  }

  sendJson(res, 200, {
    bookingId,
    status: 'PENDING',
    message: 'Tạo đơn MoMo thành công (mock).',
  });
}

function handleMomoCheckout(pathname, res) {
  const match = pathname.match(/^\/api\/payment\/momo\/checkout\/(\d+)$/);
  if (!match) {
    sendNotFound(res);
    return;
  }

  const bookingId = Number(match[1]);
  sendJson(res, 200, {
    payUrl: `${frontendBaseUrl}/payment-success?orderId=ORD-${bookingId}&requestId=REQ-${bookingId}&resultCode=0&message=Success&amount=250000&transId=TRANS-${bookingId}`,
  });
}

function handleMomoConfirm(res) {
  sendJson(res, 200, {
    status: 'success',
    message: 'MoMo confirmation mock completed.',
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
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
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

  const tripRoute = matchTrip(pathname);
  if (req.method === 'GET' && tripRoute) {
    if (tripRoute.type === 'trip') {
      const trip = findTrip(tripRoute.tripId);
      if (!trip) {
        sendNotFound(res, 'Không tìm thấy chuyến đi.');
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
      sendJson(res, 400, { message: 'Body JSON không hợp lệ.', error: error.message });
    }
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
      sendJson(res, 400, { message: 'Không thể tạo feedback mock.', error: error.message });
    }
    return;
  }

  if (req.method === 'POST' && pathname.startsWith('/api/payment/momo/checkout/')) {
    handleMomoCheckout(pathname, res);
    return;
  }

  if (req.method === 'POST' && pathname === '/api/payment/momo/confirm') {
    handleMomoConfirm(res);
    return;
  }

  sendNotFound(res);
});

server.listen(port, host, () => {
  console.log(`Mock API server is listening on http://${host}:${port}`);
});
