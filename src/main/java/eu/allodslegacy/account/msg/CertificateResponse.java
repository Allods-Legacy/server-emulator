package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;

public class CertificateResponse implements CppOutSerializable {

    private final byte[] certificate;

    public CertificateResponse(byte[] certificate) {
        this.certificate = certificate;
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeInt(this.certificate.length);
        for (byte b : certificate) {
            out.write(b);
        }
    }
}
