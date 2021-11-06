package com.jskang.storagenode.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class CommonConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        DefaultPartHttpMessageReader partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxHeadersSize(16384); // 9 KiB, default is 8 KiB
        partReader.setEnableLoggingRequestDetails(true);

        MultipartHttpMessageReader multipartReader = new MultipartHttpMessageReader(partReader);
        multipartReader.setEnableLoggingRequestDetails(true);

        configurer.defaultCodecs().multipartReader(multipartReader);
    }
}
