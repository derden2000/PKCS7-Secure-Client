package pro.antonshu.client.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pro.antonshu.client.entities.Document;

@Service
public class RestService {

    private RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T doGetForObject(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }

    public <T> T doGetForObjectAuth(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }

    public <T> T doPostForObject(String url, Document document, Class<T> responseType) {
        return restTemplate.postForObject(url, document, responseType);
    }

    public <T> T doPatchForObject2(String url, Document document, Class<T> responseType) {
        return restTemplate.patchForObject(url, document, responseType);
    }
}
