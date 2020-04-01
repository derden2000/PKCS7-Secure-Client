package pro.antonshu.client.controllers;

import com.google.gson.Gson;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pro.antonshu.client.entities.Document;
import pro.antonshu.client.services.RestService;
import pro.antonshu.client.services.pkcs7.PKCS7Signer;
import pro.antonshu.client.utils.MediaTypeUtils;
import pro.antonshu.client.utils.Packet;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class MainController {

    private RestService restService;

    private String pathToSendPost, pathToSendEncPost;

    private PKCS7Signer pkcs7Signer;

    private ServletContext servletContext;

    @Value("${rest.pathToSendPost}")
    public void setPathToSendPost(String pathToSendPost) {
        this.pathToSendPost = pathToSendPost;
    }

    @Value("${rest.pathToSendEncPost}")
    public void setPathToSendEncPost(String pathToSendEncPost) {
        this.pathToSendEncPost = pathToSendEncPost;
    }

    @Autowired
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Autowired
    public void setPkcs7Signer(PKCS7Signer pkcs7Signer) {
        this.pkcs7Signer = pkcs7Signer;
    }

    @Autowired
    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    private final String keyStoreFile = "src/main/resources/clientkeystore.p12";
    private String keyStorePassword;
    private final String pathToSendAuthRequest = "https://localhost:8443/test/hello";
    private final String pathToSendDocsRequest = "https://localhost:8443/docs/get?id=";
    private final String pathToSendFileRequest = "https://localhost:8443/docs/get/ent?id=";

    @Value("${secure.keyStorePassword}")
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @GetMapping("/")
    public String index(Model model) {
        String answer = restService.doGetForObjectAuth(pathToSendAuthRequest, String.class);
        model.addAttribute("answer", answer);
        System.out.println("Answer from REST-SERVER: " + answer);
        return "index";
    }

    @GetMapping("/docs")
    public String getDocs(Model model,
                          @RequestParam("id") Long id) {
        Gson gson = new Gson();
        Packet packet = gson.fromJson(restService.doGetForObject(pathToSendDocsRequest+id, String.class), Packet.class);
        model.addAttribute("docs", packet.getDocumentDtos());
        return "docs";
    }


    @PostMapping("/docs/view")
    public String getFile(@RequestParam("id") Long id) throws IOException {
        Document document = restService.doGetForObject(pathToSendFileRequest+id, Document.class);
        String path = "src/main/resources/files/" + document.getTitle();
        Files.write(Paths.get(path), document.getData());

        return String.format("redirect:/docs/download?fileName=%s",document.getTitle());
    }

    @RequestMapping("/docs/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @RequestParam("fileName") String fileName) throws IOException {

        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, fileName);
        File file = new File("src/main/resources/files/" + fileName);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType)
                .contentLength(file.length())
                .body(resource);
    }

    @PostMapping("/sendpkcs")
    public String submitPkcs(@RequestParam("files") MultipartFile[] files, Model model) {
        MultipartFile file = files[0];
        byte[] signedData = new byte[0];
        try {
            byte[] dataFromFile = file.getBytes();
            signedData = Base64.encode(pkcs7Signer.signPkcs7(dataFromFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document document = new Document(file.getOriginalFilename(), signedData, "https://sample.ru");
        String response = restService.doPostForObject(pathToSendEncPost, document, String.class);
        System.out.println("Response: " + response);
//        model.addAttribute("answer", response);
        return "pkcs";
    }

    @GetMapping("/pkcs")
    public String sendEncodedFile(Model model) {
        return "pkcs";
    }
}
