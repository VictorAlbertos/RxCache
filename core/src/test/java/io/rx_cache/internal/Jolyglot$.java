package io.rx_cache.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.victoralbertos.jolyglot.GsonSpeaker;
import io.victoralbertos.jolyglot.JacksonSpeaker;
import io.victoralbertos.jolyglot.Jolyglot;

public final class Jolyglot$ {
    public static Jolyglot newInstance() {
        return new GsonSpeaker();
    }

    private static JacksonSpeaker jacksonSpeaker() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return new JacksonSpeaker(objectMapper);
    }
}
