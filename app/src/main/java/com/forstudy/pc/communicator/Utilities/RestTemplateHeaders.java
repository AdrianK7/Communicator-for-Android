package com.forstudy.pc.communicator.Utilities;

import com.forstudy.pc.communicator.Utilities.RequestInterceptor;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 03.03.17.
 */

public class RestTemplateHeaders {

    public RestTemplate getRestWithHeaders(RestTemplate restTemplate, List<RequestInterceptor> headers)
    {
        if(headers != null) {
            ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.addAll(headers);
            restTemplate.setInterceptors(interceptors);
        }
        return restTemplate;
    }
}
