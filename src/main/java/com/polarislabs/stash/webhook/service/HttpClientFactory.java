package com.polarislabs.stash.webhook.service;

import org.apache.http.client.HttpClient;

/**
 * Defines a generator that will create a HttpClient
 * 
 * @author Michael Irwin (mikesir87)
 */
public interface HttpClientFactory {

  /**
   * Generate a HttpClient.
   * @param usingSsl True if using ssl.
   * @param trustAllCerts True if all certs should be trusted.
   * @return An HttpClient.
   * @throws Exception Any exception, but shouldn't happen.
   */
  HttpClient getHttpClient(Boolean usingSsl, Boolean trustAllCerts)
      throws Exception;
}