package pro.antonshu.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pro.antonshu.client.entities.Document;
import pro.antonshu.client.services.RestService;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

@Controller
public class MainController {

    private RestService restService;

    private String pathToSendPost;

    @Value("${rest.pathToSendPost}")
    public void setPathToSendPost(String pathToSendPost) {
        this.pathToSendPost = pathToSendPost;
    }

    @Autowired
    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    private final String keyStoreFile = "src/main/resources/clientkeystore.p12";
    private final String keyStorePassword = "client";

    @GetMapping("/")
    public String index(Model model) throws KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException, CertificateException {
//        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        keyStore.load(new FileInputStream(new File(keyStoreFile)),
//                keyStorePassword.toCharArray());
//
//        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
//                new SSLContextBuilder()
//                        .loadTrustMaterial(null, new TrustSelfSignedStrategy())
//                        .loadKeyMaterial(keyStore, keyStorePassword.toCharArray())
//                        .build(),
//                NoopHostnameVerifier.INSTANCE);
//
//        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(
//                socketFactory).build();
//
//        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
//                httpClient);
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
//
//        String answer = restTemplate.getForObject("https://localhost:8443/test/hello", String.class);
        String answer = restService.doGetForObject("https://localhost:8443/test/hello", String.class);
        model.addAttribute("answer", answer);
        System.out.println("Answer from REST-SERVER: " + answer);
        return "index";
    }


    @PostMapping("/send")
//    @ResponseBody
    public String submit(@RequestParam("files") MultipartFile[] files, Model model) {
        MultipartFile file = files[0];
        System.out.println("file.getName(): " + file.getName());
        System.out.println("file.getContentType(): " + file.getContentType());
        System.out.println("file.getOriginalFilename(): " + file.getOriginalFilename());
        Document document = new Document(file.getOriginalFilename(), "https://mcs.ru");
        System.out.println("Document: " + document);
        String response = restService.doPostForObject(pathToSendPost, document, String.class);
        model.addAttribute("answer", response);
        System.out.println("Response from Server: " + response);
        return "index";
    }
}
