/*
 * Copyright (C) 2008 Alexander Christian <alex(at)root1.de>. All rights reserved.
 * 
 * This file is part of SIMON.
 *
 *   SIMON is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   SIMON is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with SIMON.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.root1.simon.ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * A default implementation for a SSL powered SIMON communication.<br>
 * All that is needed is a keystore for the client and the server and the
 * corresponding password to access it.
 * 
 * @author Alexander Christian
 * @version 200901141313
 * 
 */
public class DefaultSslContextFactory implements SslContextFactory {

	private static String _KEYSTORE_TYPE_ = KeyStore.getDefaultType();
	private String pathToClientKeystore;
	private String clientKeystorePass;
	private String pathToServerKeystore;
	private String serverKeystorePass;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.root1.simon.ssl.ISslContextFactory#getServerContext()
	 */
	public SSLContext getServerContext() {
		return getContext(pathToServerKeystore, serverKeystorePass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.root1.simon.ssl.ISslContextFactory#getClientContext()
	 */
	public SSLContext getClientContext() {
		return getContext(pathToClientKeystore, clientKeystorePass);
	}

	/**
	 * Sets the needed information for creating a client {@link SSLContext}
	 * 
	 * @param pathToClientKeystore
	 *            the path to the keystore file for the client
	 * @param clientKeystorePass
	 *            the password needed to access the keystore
	 */
	public void setClientKeystore(String pathToClientKeystore,
			String clientKeystorePass) {
		this.pathToClientKeystore = pathToClientKeystore;
		this.clientKeystorePass = clientKeystorePass;
	}

	/**
	 * Sets the needed information for creating a server {@link SSLContext}
	 * 
	 * @param pathToServerKeystore
	 *            the path to the keystore file for the server
	 * @param serverKeystorePass
	 *            the password needed to access the keystore
	 */
	public void setServerKeystore(String pathToServerKeystore,
			String serverKeystorePass) {
		this.pathToServerKeystore = pathToServerKeystore;
		this.serverKeystorePass = serverKeystorePass;
	}

	private SSLContext getContext(String pathToKeystore, String keystorePass) {
		SSLContext sslContext = null;

		try {
			sslContext = SSLContext.getInstance("TLS");

			System.out.println("load key store");
			KeyStore keyStore = getKeyStore(pathToKeystore, keystorePass);

			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, keystorePass.toCharArray());

			// initialize trust manager factory
			TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(keyStore);

			sslContext.init(keyManagerFactory.getKeyManagers(),
					trustManagerFactory.getTrustManagers(), null);

		} catch (Exception e) {
			e.printStackTrace();
		}

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
