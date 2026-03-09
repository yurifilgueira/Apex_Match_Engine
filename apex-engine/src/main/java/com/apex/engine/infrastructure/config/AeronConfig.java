package com.apex.engine.infrastructure.config;

import io.aeron.Aeron;
import io.aeron.driver.MediaDriver;
import io.aeron.driver.ThreadingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AeronConfig {

    @Value("${apex-engine.aeron.directory-name:/dev/shm/aeron}")
    private String aeronDirectoryName;

    @Bean(destroyMethod = "close")
    public MediaDriver launchAeron() {
        MediaDriver.Context ctx = new MediaDriver.Context()
                .threadingMode(ThreadingMode.DEDICATED)
                .dirDeleteOnStart(true)
                .aeronDirectoryName(aeronDirectoryName);

        return MediaDriver.launchEmbedded(ctx);
    }

    @Bean(destroyMethod = "close")
    public Aeron aeron(MediaDriver mediaDriver) {
        Aeron.Context ctx = new Aeron.Context()
                .aeronDirectoryName(mediaDriver.aeronDirectoryName())
                .errorHandler(Throwable::printStackTrace);

        return Aeron.connect(ctx);
    }

}
