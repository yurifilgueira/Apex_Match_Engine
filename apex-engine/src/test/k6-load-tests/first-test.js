import http from 'k6/http';

export const options = {
    stages: [
        { duration: "30s", target: 2000 },
        { duration: "30s", target: 5000 },
        { duration: "30s", target: 0 }
    ],
};

function getRandomPrice(min, max) {
    return (Math.random() * (max - min) + min).toFixed(2);
}

function getRandomQuantity(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

export default function () {
    const host = __ENV.HTTP_HOST || 'localhost';
    const port = __ENV.HTTP_PORT || '8080';
    const url = `http://${host}:${port}/orders`;

    const payload = JSON.stringify({
        ticker: 'PETR4',
        price: parseFloat(getRandomPrice(35.00, 55.00)),
        quantity: getRandomQuantity(10, 500),
        side: Math.random() < 0.5 ? 'BID' : 'ASK'
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    http.post(url, payload, params);
}
