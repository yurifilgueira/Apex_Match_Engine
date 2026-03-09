import http from 'k6/http';

export const options = {
    stages: [
        { duration: "30s", target: 2000 },
        { duration: "30s", target: 5000 },
        { duration: "30s", target: 0 }
    ],
};

function getRandomPrice(min, max) {
    return Math.floor(Math.random() * (max - min) + min);
}

function getRandomQuantity(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

export default function () {
    const host = __ENV.HTTP_HOST || 'localhost';
    const url = `http://${host}:8080/orders`;

    const params = {
        headers: {
            'Content-Type': 'application/octet-stream',
        },
    };

    const buffer = new ArrayBuffer(29);
    const view = new DataView(buffer);

    view.setUint16(0, 21, true);
    view.setUint16(2, 1, true);
    view.setUint16(4, 1, true);
    view.setUint16(6, 1, true);

    const ticker = 'PETR4   ';
    for (let i = 0; i < 8; i++) {
        view.setUint8(8 + i, ticker.charCodeAt(i));
    }

    const price = getRandomPrice(3500, 5500);
    view.setUint32(16, price, true);
    view.setUint32(20, 0, true);

    const quantity = getRandomQuantity(10, 500);
    view.setUint32(24, quantity, true);

    const side = Math.random() < 0.5 ? 0 : 1;
    view.setUint8(28, side);

    http.post(url, buffer, params);
}
