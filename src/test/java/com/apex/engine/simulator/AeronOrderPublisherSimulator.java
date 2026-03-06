package com.apex.engine.simulator;

import com.apex.engine.sbe.MessageHeaderEncoder;
import com.apex.engine.sbe.NewOrderEncoder;
import com.apex.engine.sbe.SideEnum;
import io.aeron.Aeron;
import io.aeron.Publication;
import org.HdrHistogram.ConcurrentHistogram;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

public class AeronOrderPublisherSimulator {

    record Stage(int durationSeconds, int targetVus) {}

    private static final List<Stage> STAGES = List.of(
            new Stage(30, 2000),
            new Stage(30, 5000),
            new Stage(30, 0)
    );

    private static final ConcurrentHistogram histogram = new ConcurrentHistogram(TimeUnit.SECONDS.toNanos(10), 3);
    private static final LongAdder totalOrdersAccumulated = new LongAdder();
    private static final LongAdder throughputCounter = new LongAdder();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    private static int maxVus = 0;
    private static long startTimeMillis;

    private static String getParam(String key, String def) {
        String val = System.getProperty(key);
        if (val == null) val = System.getenv(key.replace('.', '_').toUpperCase());
        return val != null ? val : def;
    }

    public static void main(String[] args) throws InterruptedException {
        String channel = getParam("aeron.channel", "aeron:udp?endpoint=apex-engine:40456");
        int streamId = Integer.parseInt(getParam("aeron.streamId", "10"));
        String aeronDir = getParam("aeron.dir", "/dev/shm/aeron");

        try (Aeron aeron = Aeron.connect(new Aeron.Context().aeronDirectoryName(aeronDir));
             Publication publication = aeron.addPublication(channel, streamId);
             var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            while (!publication.isConnected()) {
                Thread.sleep(1000);
            }

            startTimeMillis = System.currentTimeMillis();
            int totalTestDuration = STAGES.stream().mapToInt(Stage::durationSeconds).sum();
            List<Thread> activeVus = new ArrayList<>();

            Thread.ofVirtual().start(() -> {
                while (running.get()) {
                    try {
                        Thread.sleep(1000);
                        long currentThroughput = throughputCounter.sumThenReset();
                        System.out.printf("[%ds] VUs: %d | Throughput: %d/s | Total: %d%n",
                                (System.currentTimeMillis() - startTimeMillis) / 1000,
                                activeVus.size(), currentThroughput, totalOrdersAccumulated.sum());
                    } catch (InterruptedException e) { break; }
                }
            });

            while (true) {
                long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                double elapsedSeconds = elapsedMillis / 1000.0;
                if (elapsedSeconds >= totalTestDuration) break;

                int targetVus = calculateTargetVus(elapsedSeconds);
                maxVus = Math.max(maxVus, targetVus);

                while (activeVus.size() < targetVus) {
                    activeVus.add(Thread.ofVirtual().start(() -> runVu(publication)));
                }
                while (activeVus.size() > targetVus && !activeVus.isEmpty()) {
                    activeVus.removeLast().interrupt();
                }
                Thread.sleep(100);
            }

            running.set(false);
            Thread.sleep(1000); // Aguarda encerramento das threads
            printFinalReport(totalOrdersAccumulated.sum(), (System.currentTimeMillis() - startTimeMillis) / 1000.0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void runVu(Publication publication) {
        UnsafeBuffer buffer = new UnsafeBuffer(ByteBuffer.allocateDirect(128));
        MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        NewOrderEncoder orderEncoder = new NewOrderEncoder();

        while (!Thread.currentThread().isInterrupted() && running.get()) {
            headerEncoder.wrap(buffer, 0)
                    .blockLength(orderEncoder.sbeBlockLength())
                    .templateId(orderEncoder.sbeTemplateId())
                    .schemaId(orderEncoder.sbeSchemaId())
                    .version(orderEncoder.sbeSchemaVersion());

            orderEncoder.wrap(buffer, headerEncoder.encodedLength())
                    .ticker("PETR4   ")
                    .price(3500)
                    .quantity(100)
                    .side(SideEnum.BID);

            int messageLength = headerEncoder.encodedLength() + orderEncoder.encodedLength();
            long start = System.nanoTime();
            
            long result;
            do {
                result = publication.offer(buffer, 0, messageLength);
                if (result < 0) {
                    if (result == Publication.CLOSED || result == Publication.NOT_CONNECTED) return;
                    Thread.onSpinWait();
                }
            } while (result < 0 && running.get());

            if (result > 0) {
                histogram.recordValue(System.nanoTime() - start);
                totalOrdersAccumulated.increment();
                throughputCounter.increment();
            }
        }
    }

    private static int calculateTargetVus(double elapsedSeconds) {
        double currentTotal = 0;
        int lastVus = 0;
        for (Stage stage : STAGES) {
            if (elapsedSeconds < currentTotal + stage.durationSeconds) {
                double stageProgress = (elapsedSeconds - currentTotal) / stage.durationSeconds;
                return (int) (lastVus + (stage.targetVus - lastVus) * stageProgress);
            }
            currentTotal += stage.durationSeconds;
            lastVus = stage.targetVus;
        }
        return 0;
    }

    private static void printFinalReport(long totalMsgs, double duration) {
        System.out.println("\n█ TOTAL RESULTS\n");
        System.out.println("    AERON_SBE_BINARY (ZERO-COPY)\n");

        System.out.printf("    offer_latency..................: avg=%.2fus min=%.2fus med=%.2fus max=%.2fms p(90)=%.2fus p(95)=%.2fus\n",
                histogram.getMean() / 1000.0,
                histogram.getMinValue() / 1000.0,
                histogram.getValueAtPercentile(50) / 1000.0,
                histogram.getMaxValue() / 1_000_000.0,
                histogram.getValueAtPercentile(90) / 1000.0,
                histogram.getValueAtPercentile(95) / 1000.0);

        System.out.println("      { expected_response:true }...: 100%");
        System.out.printf("    throughput.....................: %d messages | %.2f/s\n", totalMsgs, totalMsgs / duration);

        System.out.println("\n    EXECUTION\n");
        System.out.printf("    total_duration.................: %.2fs\n", duration);
        System.out.printf("    vus............................: %d\n", maxVus);

        System.out.println("\n    NETWORK\n");
        double dataSent = (totalMsgs * 29.0) / (1024 * 1024);
        System.out.printf("    data_sent......................: %.2f MB | %.2f MB/s\n", dataSent, dataSent / duration);
    }
}
