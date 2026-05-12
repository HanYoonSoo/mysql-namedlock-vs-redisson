package com.lock.mysql_namedlock_vs_redisson.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestUtil {

    private static final RestTemplate REST_TEMPLATE = create();

    public static Integer post(String uri,
                               Object... uriVariables) {
        return REST_TEMPLATE.postForObject(uri, null, Integer.class, uriVariables);
    }

    /**
     * post 로 동시 요청한다.
     */
    public static void concurrentPost(int threadCount,
                                      String uri,
                                      Object... uriVariables) {
        concurrentPost(threadCount, uri, ignored -> uriVariables);
    }

    /**
     * 요청마다 다른 uriVariables 로 post 를 동시 요청한다.
     */
    public static void concurrentPost(int threadCount,
                                      String uri,
                                      IntFunction<Object[]> uriVariablesFactory) {

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int requestIndex = i;
            executorService.submit(() -> {
                try {
                    barrier.await();
                    Integer count = post(uri, uriVariablesFactory.apply(requestIndex));
                    if (count != null) {
                        log.info("response count : {}", count);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
    }

    private static RestTemplate create() {
        return new RestTemplateBuilder().errorHandler(new ErrorHandler()).build();
    }

    private static class ErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().isError();
        }

        @Override
        public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
            String message = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            log.error("request failed. status={}, body={}", response.getStatusCode(), message);
        }

    }

}
