package io.rx_cache2.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.victoralbertos.jolyglot.JacksonSpeaker;
import io.victoralbertos.jolyglot.JolyglotGenerics;
import io.victoralbertos.jolyglot.MoshiSpeaker;

public final class Jolyglot$ {
  public static JolyglotGenerics newInstance() {
    return new MoshiSpeaker();
  }

  private static JacksonSpeaker jacksonSpeaker() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    return new JacksonSpeaker(objectMapper);
  }
}
