package priviot.utils.data;

/**
 * Encapsulates the parameters needed for encryption.
 */
public class EncryptionParameters {
    private String symmetricEncryptionAlgorithm;
    private int symmetricEncryptionKeySize; 
    private String asymmetricEncryptionAlgorithm;
    private int asymmetricEncryptionKeySize;
    
    public EncryptionParameters(String symmetricEncryptionAlgorithm, 
                                int symmetricEncryptionKeySize,
                                String asymmetricEncryptionAlgorithm,
                                int asymmetricEncryptionKeySize) {
        this.symmetricEncryptionAlgorithm = symmetricEncryptionAlgorithm;
        this.symmetricEncryptionKeySize = symmetricEncryptionKeySize;
        this.asymmetricEncryptionAlgorithm = asymmetricEncryptionAlgorithm;
        this.asymmetricEncryptionKeySize = asymmetricEncryptionKeySize;
    }

    public String getSymmetricEncryptionAlgorithm() {
        return symmetricEncryptionAlgorithm;
    }

    public void setSymmetricEncryptionAlgorithm(
            String symmetricEncryptionAlgorithm) {
        this.symmetricEncryptionAlgorithm = symmetricEncryptionAlgorithm;
    }

    public int getSymmetricEncryptionKeySize() {
        return symmetricEncryptionKeySize;
    }

    public void setSymmetricEncryptionKeySize(int symmetricEncryptionKeySize) {
        this.symmetricEncryptionKeySize = symmetricEncryptionKeySize;
    }

    public String getAsymmetricEncryptionAlgorithm() {
        return asymmetricEncryptionAlgorithm;
    }

    public void setAsymmetricEncryptionAlgorithm(
            String asymmetricEncryptionAlgorithm) {
        this.asymmetricEncryptionAlgorithm = asymmetricEncryptionAlgorithm;
    }

    public int getAsymmetricEncryptionKeySize() {
        return asymmetricEncryptionKeySize;
    }

    public void setAsymmetricEncryptionKeySize(int asymmetricEncryptionKeySize) {
        this.asymmetricEncryptionKeySize = asymmetricEncryptionKeySize;
    }
}
