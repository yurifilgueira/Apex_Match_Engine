import grpc from 'k6/net/grpc';
import { check, sleep } from 'k6';

const client = new grpc.Client();

// Carrega o arquivo .proto
// Caminho ajustado para funcionar tanto local quanto no Docker (via volume)
// No Docker, montamos em /main/resources/proto
// Localmente, está em ../../main/resources/proto
// Vamos tentar carregar de um caminho que funcione no Docker primeiro
try {
    client.load(['/main/resources/proto'], 'order_service.proto');
} catch (e) {
    client.load(['../../main/resources/proto'], 'order_service.proto');
}

export const options = {
    stages: [
        { duration: '100s', target: 100 },
        // { duration: '1m', target: 500 },
        // { duration: '10s', target: 0 },
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
    // Pega o host da variável de ambiente ou usa host.docker.internal (para Docker)
    // Se estiver rodando localmente fora do Docker, pode ser necessário definir GRPC_HOST='localhost'
    const host = __ENV.GRPC_HOST || 'host.docker.internal';
    
    if (client.state !== 'READY') {
        client.connect(`${host}:9090`, {
            plaintext: true
        });
    }

    const data = {
        ticker: 'PETR4',
        price_scaled: getRandomPriceScaled(35.00, 55.00),
        quantity: getRandomQuantity(10, 500),
        side: getRandomSide()
    };

    const response = client.invoke('com.apex.engine.v1.OrderServiceGrpc/CreateOrder', data);

    // console.log(response);

    check(response, {
        'status is OK': (r) => r && r.status === grpc.StatusOK,
    });
}
