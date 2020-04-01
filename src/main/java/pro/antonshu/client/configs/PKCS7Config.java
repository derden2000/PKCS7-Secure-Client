package pro.antonshu.client.configs;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PKCS7Config {

    private static final String PATH_TO_KEYSTORE = "src/main/resources/clientkeystore.p12";
    private static final String KEY_ALIAS_IN_KEYSTORE = "client-cert";
    private String KEYSTORE_PASSWORD;
    private static final String SIGNATUREALGO = "SHA1withRSA";

    @Value("${secure.keyStorePassword}")
    public void setKEYSTORE_PASSWORD(String KEYSTORE_PASSWORD) {
        this.KEYSTORE_PASSWORD = KEYSTORE_PASSWORD;
    }

    @Bean
    KeyStore loadKeyStore() throws Exception {

        KeyStore keystore = KeyStore.getInstance("JKS"); //KeyStore.getDefaultType()
        InputStream is = new FileInputStream(PATH_TO_KEYSTORE);
        keystore.load(is, KEYSTORE_PASSWORD.toCharArray());
        return keystore;
    }

    @Bean
    CMSSignedDataGenerator setUpProvider() throws Exception {
        KeyStore keyStore = loadKeyStore();
        Security.addProvider(new BouncyCastleProvider());
        Certificate[] certificateChain = (Certificate[]) keyStore.getCertificateChain(KEY_ALIAS_IN_KEYSTORE);
        final List<Certificate> certificateList = new ArrayList<Certificate>();
        for (int i = 0, length = certificateChain == null ? 0 : certificateChain.length; i < length; i++) {
            certificateList.add(certificateChain[i]);
        }

        Store certStore = new JcaCertStore(certificateList);
        Certificate cert = keyStore.getCertificate(KEY_ALIAS_IN_KEYSTORE);
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATUREALGO).setProvider("BC").
                build((PrivateKey) (keyStore.getKey(KEY_ALIAS_IN_KEYSTORE, KEYSTORE_PASSWORD.toCharArray())));
        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").
                build()).build(signer, (X509Certificate) cert));
        generator.addCertificates(certStore);
        return generator;
    }
}
