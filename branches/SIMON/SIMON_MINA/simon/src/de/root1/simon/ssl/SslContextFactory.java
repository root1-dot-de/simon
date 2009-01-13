package de.root1.simon.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SslContextFactory {
	
	private static String _KEYSTORE_TYPE_ = KeyStore.getDefaultType();

	public SSLContext createServerContext() throws NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		String pathToKeyStore;
		String keyStorePwd;
		
		pathToKeyStore = "keys/.keystore";
		keyStorePwd = "planet";
		
		SSLContext sslContext = SSLContext.getInstance("TLS");

		System.out.println("load key store");
		KeyStore keyStore = getKeyStore(pathToKeyStore, keyStorePwd);

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, keyStorePwd.toCharArray());

		// initialize trust manager factory
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		
		return sslContext;
	}
	
	public static SSLContext createClientContext() throws NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException{
		
		String pathToKeyStore;
		String keyStorePwd;
		
		pathToKeyStore = "keys/.keystore";
		keyStorePwd = "planet";
		
		SSLContext sslContext = SSLContext.getInstance("TLS");

		System.out.println("load key store");
		KeyStore keyStore = getKeyStore(pathToKeyStore, keyStorePwd);

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, keyStorePwd.toCharArray());

		// initialize trust manager factory
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
		
		return sslContext;
	}
	
	private static KeyStore getKeyStore(String aPath, String aPassword)
			throws FileNotFoundException, KeyStoreException, IOException,
			NoSuchAlgorithmException, CertificateException {
		KeyStore store;
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(aPath);
			store = KeyStore.getInstance(_KEYSTORE_TYPE_);
			store.load(fin, aPassword != null ? aPassword.toCharArray() : null);

		} finally {
			fin.close();
		}
		return store;
	}

}
