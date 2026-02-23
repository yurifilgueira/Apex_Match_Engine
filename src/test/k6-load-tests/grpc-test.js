import grpc from 'k6/net/grpc';
import { check, sleep } from 'k6';

const client = new grpc.Client();
client.load(['/proto'], 'order_service.proto');

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '2m', target: 500 },
        { duration: '5m', target: 500 },
    ],
};

function getRandomPriceScaled(min, max) {
    const price = Math.random() * (max - min) + min;
    return Math.floor(price * 100);
}

function getRandomQuantity(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
}

function getRandomSide() {
    return Math.random() > 0.5 ? 1 : 2;
}

export default function () {
    const host = __ENV.GRPC_HOST || 'host.docker.internal';
    
    client.connect(`${host}:9090`, {
        plaintext: true
    });

    const data = {
        ticker: 'PETR4',
        price_scaled: getRandomPriceScaled(35.00, 55.00),
        quantity: getRandomQuantity(10, 500),
        side: getRandomSide()
    };

    const response = client.invoke('com.apex.engine.v1.OrderServiceGrpc/CreateOrder', data);

    check(response, {
        'status is OK': (r) => r && r.status === grpc.StatusOK,
    });
    
    client.close();
}