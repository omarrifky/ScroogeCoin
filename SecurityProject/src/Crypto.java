import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;


public class Crypto {
	// Credited to https://niels.nu/blog/2016/java-rsa.html For encryption
	// Credited to https://github.com/TrojanCookieProtocol/BlockchainSeries1 for Hashing
	public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
	    Cipher encryptCipher = Cipher.getInstance("RSA");
	    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

	    byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

	    return Base64.getEncoder().encodeToString(cipherText);
	}
	public static KeyPair generateKeyPair() throws Exception {
	    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
	    generator.initialize(2048, new SecureRandom());
	    KeyPair pair = generator.generateKeyPair();

	    return pair;
	}
	public static String computeHash(String data) {
		
		String dataToHash = data;
		
		MessageDigest digest;
		String encoded = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
			encoded = Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return encoded;
		
	}
	public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
	    byte[] bytes = Base64.getDecoder().decode(cipherText);

	    Cipher decriptCipher = Cipher.getInstance("RSA");
	    decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

	    return new String(decriptCipher.doFinal(bytes), StandardCharsets.UTF_8);
	}
	public static String sign(String plainText, PrivateKey privateKey) throws Exception {
	    Signature privateSignature = Signature.getInstance("SHA256withRSA");
	    privateSignature.initSign(privateKey);
	    privateSignature.update(plainText.getBytes(StandardCharsets.UTF_8));

	    byte[] signature = privateSignature.sign();

	    return Base64.getEncoder().encodeToString(signature);
	}
	public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
	    Signature publicSignature = Signature.getInstance("SHA256withRSA");
	    publicSignature.initVerify(publicKey);
	    publicSignature.update(plainText.getBytes(StandardCharsets.UTF_8));

	    byte[] signatureBytes = Base64.getDecoder().decode(signature);

	    return publicSignature.verify(signatureBytes);
	}
}
