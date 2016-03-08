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

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default implementation for a SSL powered SIMON communication.<br>
 * All that is needed is a keystore and the corresponding password to access it.
 *
 * @author Alexander Christian
 * @version 200901191432
 *
 */
public class DefaultSslContextFactory implements SslContextFactory {

    /**
     * TODO document me
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String _KEYSTORE_TYPE_ = KeyStore.getDefaultType();

    private SSLContext sslContext;

    /**
     * Sets the needed information for creating the {@link SSLContext}
     *
     * @param pathToKeystore the path to the keystore file for the server
     * @param keystorePass the password needed to access the keystore
     * @throws NoSuchAlgorithmException if no Provider supports a TLS
     * @throws IOException if there's a problem while readinting the
     * keystorefile
     * @throws CertificateException if any of the certificates in the keystore
     * could not be loaded
     * @throws KeyStoreException if no Provider supports a KeyStoreSpi
     * implementation for the default keystore type (see:
     * KeyStore.getDefaultType())
     * @throws FileNotFoundException if the keystore file was not found
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException if initializing the context fails
     */
    public DefaultSslContextFactory(String pathToKeystore, String keystorePass) throws NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

        if (pathToKeystore == null) {
            throw new IllegalArgumentException("path to keystore cannot be null");
        }
        if (keystorePass == null) {
            throw new IllegalArgumentException("keystorePass cannot be null. Maybe \"\", but not null.");
        }

        sslContext = null;

        sslContext = SSLContext.getInstance("TLS");

        logger.debug("loading key store");
        KeyStore keyStore = getKeyStore(pathToKeystore, keystorePass);
        logger.debug("keystore loaded");

        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePass.toCharArray());
        logger.debug("keymanager factory initialized");

        // initialize trust manager factory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        logger.debug("trust manager factory factory initialized");

        sslContext.init(keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(), null);

        logger.debug("ssl context initialized");

    }

    /**
     * Sets the needed information for creating the {@link SSLContext}
     *
     * @param keystoreStream an inputstream for reading the keystore
     * @param keystorePass the password needed to access the keystore
     * @throws NoSuchAlgorithmException if no Provider supports a TLS
     * @throws IOException if there's a problem while readinting the
     * keystorefile
     * @throws CertificateException if any of the certificates in the keystore
     * could not be loaded
     * @throws KeyStoreException if no Provider supports a KeyStoreSpi
     * implementation for the default keystore type (see:
     * KeyStore.getDefaultType())
     * @throws FileNotFoundException if the keystore file was not found
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException if initializing the context fails
     */
    public DefaultSslContextFactory(InputStream keystoreStream, String keystorePass) throws NoSuchAlgorithmException, FileNotFoundException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
        if (keystoreStream == null) {
            throw new IllegalArgumentException("keystoreStream cannot be null");
        }
        if (keystorePass == null) {
            throw new IllegalArgumentException("keystorePass cannot be null. Maybe \"\", but not null.");
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(keystoreStream, keystorePass.toCharArray());
        trustManagerFactory.init(keystore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        sslContext = SSLContext.getInstance("TLS");
        logger.debug("trust manager factory factory initialized");
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystore, keystorePass.toCharArray());
        logger.debug("keymanager factory initialized");

        sslContext.init(kmf.getKeyManagers(), trustManagers, null);
        logger.debug("ssl context initialized");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.root1.simon.ssl.ISslContextFactory#getSslContext()
     */
    @Override
    public SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * Loads the keystore
     *
     * @param aPath path to the keystore
     * @param aPassword password for the keystore
     * @return the loaded keystore
     * @throws FileNotFoundException if the keystore file was not found
     * @throws KeyStoreException if no Provider supports a KeyStoreSpi
     * implementation for the default keystore type (see:
     * KeyStore.getDefaultType())
     * @throws IOException if there's a problem while readinting the
     * keystorefile
     * @throws NoSuchAlgorithmException if the algorithm used to check the
     * integrity of the keystore cannot be found
     * @throws CertificateException if any of the certificates in the keystore
     * could not be loaded
     */
    private static KeyStore getKeyStore(String aPath, String aPassword)
            throws FileNotFoundException, KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException {

        KeyStore store = null;
        FileInputStream fin = null;

        try {
            fin = new FileInputStream(aPath);
            store = KeyStore.getInstance(_KEYSTORE_TYPE_);
            store.load(fin, aPassword != null ? aPassword.toCharArray() : null);

        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("the keystorefile specified by the path '" + aPath + "' was not found.");
        } catch (EOFException e) {
            throw new KeyStoreException("An error occured while loading the keystore file. Error was: " + e);
        } finally {
            if (fin != null) {
                fin.close();
            }
        }
        return store;
    }

}
