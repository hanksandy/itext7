/*

    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.signatures;

import com.itextpdf.commons.bouncycastle.cert.ocsp.IBasicOCSPResp;
import com.itextpdf.commons.bouncycastle.tsp.ITimeStampToken;
import com.itextpdf.commons.utils.DateTimeUtil;
import com.itextpdf.signatures.exceptions.SignExceptionMessageConstant;
import com.itextpdf.signatures.logs.SignLogMessageConstant;

import java.security.cert.CertificateEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyStore;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;


/**
 * This class consists of some methods that allow you to verify certificates.
 */
public class CertificateVerification {
    public static final String HAS_UNSUPPORTED_EXTENSIONS = "Has unsupported critical extension";
    public static final String CERTIFICATE_REVOKED = "Certificate revoked";


    /**
     * The Logger instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateVerification.class);

    /**
     * Verifies a single certificate for the current date.
     *
     * @param cert the certificate to verify
     * @param crls the certificate revocation list or <CODE>null</CODE>
     * @return a <CODE>String</CODE> with the error description or <CODE>null</CODE>
     * if no error
     */
    public static String verifyCertificate(X509Certificate cert, Collection<CRL> crls) {
        return verifyCertificate(cert, crls, DateTimeUtil.getCurrentTimeCalendar());
    }

    /**
     * Verifies a single certificate.
     *
     * @param cert the certificate to verify
     * @param crls the certificate revocation list or <CODE>null</CODE>
     * @param calendar the date, shall not be null
     * @return a <CODE>String</CODE> with the error description or <CODE>null</CODE>
     * if no error
     */
    public static String verifyCertificate(X509Certificate cert, Collection<CRL> crls, Calendar calendar) {
        if (SignUtils.hasUnsupportedCriticalExtension(cert)) {
            return CertificateVerification.HAS_UNSUPPORTED_EXTENSIONS;
        }
        try {
            cert.checkValidity(calendar.getTime());
        } catch (Exception e) {
            return e.getMessage();
        }
        if (crls != null) {
            for (CRL crl : crls) {
                if (crl.isRevoked(cert)) {
                    return CertificateVerification.CERTIFICATE_REVOKED;
                }
            }
        }
        return null;
    }


    /**
     * Verifies a certificate chain against a KeyStore for the current date.
     *
     * @param certs    the certificate chain
     * @param keystore the <CODE>KeyStore</CODE>
     * @param crls     the certificate revocation list or <CODE>null</CODE>
     * @return empty list if the certificate chain could be validated or a
     * <CODE>Object[]{cert,error}</CODE> where <CODE>cert</CODE> is the
     * failed certificate and <CODE>error</CODE> is the error message
     * @throws CertificateEncodingException if an encoding error occurs in {@link Certificate}.
     */
    public static List<VerificationException> verifyCertificates(Certificate[] certs, KeyStore keystore,
            Collection<CRL> crls) throws CertificateEncodingException {
        return verifyCertificates(certs, keystore, crls, DateTimeUtil.getCurrentTimeCalendar());
    }

    /**
     * Verifies a certificate chain against a KeyStore.
     *
     * @param certs    the certificate chain
     * @param keystore the <CODE>KeyStore</CODE>
     * @param crls     the certificate revocation list or <CODE>null</CODE>
     * @param calendar the date, shall not be null
     * @return empty list if the certificate chain could be validated or a
     * <CODE>Object[]{cert,error}</CODE> where <CODE>cert</CODE> is the
     * failed certificate and <CODE>error</CODE> is the error message
     * @throws CertificateEncodingException if an encoding error occurs in {@link Certificate}.
     */
    public static List<VerificationException> verifyCertificates(Certificate[] certs, KeyStore keystore,
            Collection<CRL> crls, Calendar calendar) throws CertificateEncodingException {
        List<VerificationException> result = new ArrayList<>();
        for (int k = 0; k < certs.length; ++k) {
            X509Certificate cert = (X509Certificate) certs[k];
            String err = verifyCertificate(cert, crls, calendar);
            if (err != null) {
                result.add(new VerificationException(cert, err));
            }
            try {
                for (X509Certificate certStoreX509 : SignUtils.getCertificates(keystore)) {
                    try {
                        if (verifyCertificate(certStoreX509, crls, calendar) != null) {
                            continue;
                        }
                        try {
                            cert.verify(certStoreX509.getPublicKey());
                            return result;
                        } catch (Exception e) {
                            // do nothing and continue
                        }
                    } catch (Exception ex) {
                        // Do nothing.
                    }
                }
            } catch (Exception e) {
                // Do nothing.
            }
            int j;
            for (j = 0; j < certs.length; ++j) {
                if (j == k) {
                    continue;
                }
                X509Certificate certNext = (X509Certificate) certs[j];
                try {
                    cert.verify(certNext.getPublicKey());
                    break;
                } catch (Exception e) {
                    // Do nothing.
                }
            }
            if (j == certs.length) {
                result.add(new VerificationException(cert,
                        SignExceptionMessageConstant.CANNOT_BE_VERIFIED_CERTIFICATE_CHAIN));
            }
        }
        if (result.size() == 0) {
            result.add(new VerificationException((Certificate) null,
                    SignExceptionMessageConstant.INVALID_STATE_WHILE_CHECKING_CERT_CHAIN));
        }
        return result;
    }

    /**
     * Verifies a certificate chain against a KeyStore for the current date.
     *
     * @param certs    the certificate chain
     * @param keystore the <CODE>KeyStore</CODE>
     * @return <CODE>null</CODE> if the certificate chain could be validated or a
     * <CODE>Object[]{cert,error}</CODE> where <CODE>cert</CODE> is the
     * failed certificate and <CODE>error</CODE> is the error message
     * @throws CertificateEncodingException if an encoding error occurs in {@link Certificate}.
     */
    public static List<VerificationException> verifyCertificates(Certificate[] certs, KeyStore keystore)
            throws CertificateEncodingException {
        return verifyCertificates(certs, keystore, DateTimeUtil.getCurrentTimeCalendar());
    }

    /**
     * Verifies a certificate chain against a KeyStore.
     *
     * @param certs    the certificate chain
     * @param keystore the <CODE>KeyStore</CODE>
     * @param calendar the date, shall not be null
     * @return <CODE>null</CODE> if the certificate chain could be validated or a
     * <CODE>Object[]{cert,error}</CODE> where <CODE>cert</CODE> is the
     * failed certificate and <CODE>error</CODE> is the error message
     * @throws CertificateEncodingException if an encoding error occurs in {@link Certificate}.
     */
    public static List<VerificationException> verifyCertificates(Certificate[] certs, KeyStore keystore,
            Calendar calendar) throws CertificateEncodingException {
        return verifyCertificates(certs, keystore, null, calendar);
    }

    /**
     * Verifies an OCSP response against a KeyStore.
     *
     * @param ocsp     the OCSP response
     * @param keystore the <CODE>KeyStore</CODE>
     * @param provider the provider or <CODE>null</CODE> to use the BouncyCastle provider
     * @return <CODE>true</CODE> is a certificate was found
     */
    public static boolean verifyOcspCertificates(IBasicOCSPResp ocsp, KeyStore keystore, String provider) {
        List<Exception> exceptionsThrown = new ArrayList<>();
        try {
            for (X509Certificate certStoreX509 : SignUtils.getCertificates(keystore)) {
                try {
                    if (SignUtils.isSignatureValid(ocsp, certStoreX509, provider)) {
                        return true;
                    }
                } catch (Exception ex) {
                    exceptionsThrown.add(ex);
                }
            }
        } catch (Exception e) {
            exceptionsThrown.add(e);
        }

        logExceptionMessages(exceptionsThrown);
        return false;
    }

    /**
     * Verifies a time stamp against a KeyStore.
     *
     * @param ts       the time stamp
     * @param keystore the <CODE>KeyStore</CODE>
     * @param provider the provider or <CODE>null</CODE> to use the BouncyCastle provider
     * @return <CODE>true</CODE> is a certificate was found
     */
    public static boolean verifyTimestampCertificates(ITimeStampToken ts, KeyStore keystore, String provider) {
        List<Exception> exceptionsThrown = new ArrayList<>();
        try {
            for (X509Certificate certStoreX509 : SignUtils.getCertificates(keystore)) {
                try {
                    SignUtils.isSignatureValid(ts, certStoreX509, provider);
                    return true;
                } catch (Exception ex) {
                    exceptionsThrown.add(ex);
                }
            }
        } catch (Exception e) {
            exceptionsThrown.add(e);
        }

        logExceptionMessages(exceptionsThrown);
        return false;
    }

    private static void logExceptionMessages(List<Exception> exceptionsThrown) {
        for (Exception ex : exceptionsThrown) {
            LOGGER.error(ex.getMessage() == null
                        ? SignLogMessageConstant.EXCEPTION_WITHOUT_MESSAGE
                        : ex.getMessage(),
                    ex);
        }
    }
}
