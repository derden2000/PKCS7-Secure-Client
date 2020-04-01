package pro.antonshu.client.services.pkcs7;


import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PKCS7Signer {

    private CMSSignedDataGenerator generator;

    @Autowired
    public void setGenerator(CMSSignedDataGenerator generator) {
        this.generator = generator;
    }

    public byte[] signPkcs7(final byte[] content) throws Exception {
        CMSTypedData cmsData = new CMSProcessableByteArray(content);
        CMSSignedData signedData = generator.generate(cmsData, true);
        return signedData.getEncoded();
    }
}
