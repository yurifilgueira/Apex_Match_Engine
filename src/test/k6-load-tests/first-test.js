import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 1 },
        { duration: '2m', target: 500 },
        { duration: '1m', target: 0 },
    ],
};

function getRandomPrice(min, max) {
    return (Math.random() * (max - min) + min).toFixed(2);
}

function getRandomQuantity(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

function getRandomSide() {
    const sides = ['BID', 'ASK'];
    return sides[Math.floor(Math.random() * sides.length)];
}

export default function () {
    const url = "http://localhost:8080/orders";

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const order = JSON.stringify({
        'ticker': 'PETR4',
        'price': parseFloat(getRandomPrice(35.00, 55.00)),
        'side': getRandomSide(),
        'quantity': getRandomQuantity(10, 500)
    });

    http.post(url, order, params);
}