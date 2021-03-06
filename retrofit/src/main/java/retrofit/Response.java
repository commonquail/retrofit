/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.lang.reflect.Type;
import retrofit.converter.Converter;

import static retrofit.Utils.checkNotNull;

/**
 * TODO
 */
public final class Response<T> {
  /**
   * TODO
   */
  public static <T, B extends T> Response<T> fromBody(B body) {
    return fromBody(body, new com.squareup.okhttp.Response.Builder() //
        .code(200)
        .protocol(Protocol.HTTP_1_1)
        .request(new com.squareup.okhttp.Request.Builder().url(HttpUrl.parse("http://localhost"))
            .build())
        .build());
  }

  /**
   * TODO
   */
  public static <T, B extends T> Response<T> fromBody(B body,
      com.squareup.okhttp.Response rawResponse) {
    return new Response<T>(rawResponse, body, null, null);
  }

  /**
   * TODO
   */
  public static Response<Object> fromError(int code, ResponseBody body, Converter converter) {
    return fromError(new com.squareup.okhttp.Response.Builder() //
        .code(code)
        .protocol(Protocol.HTTP_1_1)
        .body(body)
        .request(new com.squareup.okhttp.Request.Builder().url(HttpUrl.parse("http://localhost"))
            .build())
        .build(), converter);
  }

  /**
   * TODO
   */
  public static Response<Object> fromError(com.squareup.okhttp.Response rawResponse,
      Converter converter) {
    ResponseBody errorBody = rawResponse.body();
    if (errorBody == null) throw new IllegalArgumentException("Raw response must have body.");
    rawResponse = rawResponse.newBuilder().body(null).build();
    return new Response<>(rawResponse, null, errorBody, converter);
  }

  private final com.squareup.okhttp.Response rawResponse;
  private final T body;
  private final ResponseBody errorBody;
  private final Converter converter;

  Response(com.squareup.okhttp.Response rawResponse, T body, ResponseBody errorBody,
      Converter converter) {
    this.rawResponse = checkNotNull(rawResponse, "rawResponse == null");
    this.body = body;
    this.errorBody = errorBody;
    this.converter = converter;
  }

  /** The raw response from the HTTP client. */
  public com.squareup.okhttp.Response raw() {
    return rawResponse;
  }

  /** HTTP status code. */
  public int code() {
    return rawResponse.code();
  }

  public Headers headers() {
    return rawResponse.headers();
  }

  /** {@code true} if {@link #code()} is in the range [200..300). */
  public boolean isSuccess() {
    return rawResponse.isSuccessful();
  }

  /** The deserialized response body of a {@linkplain #isSuccess() successful} response. */
  public T body() {
    return body;
  }

  /** The raw response body of an {@linkplain #isSuccess() unsuccessful} response. */
  public ResponseBody errorBody() {
    return errorBody;
  }

  /**
   * The deserialize the response body of an {@linkplain #isSuccess() unsuccessful} response to
   * {@code errorType}.
   * TODO warning about generics
   */
  @SuppressWarnings("unchecked") // Caller assumes responsibility for correct cast.
  public <E> E errorBodyAs(Type errorType) {
    try {
      return (E) converter.fromBody(errorBody, errorType);
    } catch (IOException e) {
      throw new AssertionError(e); // Body is buffered.
    }
  }
}
