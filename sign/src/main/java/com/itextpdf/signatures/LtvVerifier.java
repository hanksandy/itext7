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

import com.itextpdf.bouncycastleconnector.BouncyCastleFactoryCreator;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.bouncycastle.IBouncyCastleFactory;
import com.itextpdf.commons.bouncycastle.cert.ocsp.IOCSPResp;
import com.itextpdf.commons.bouncycastle.cert.ocsp.AbstractOCSPException;
import com.itextpdf.commons.bouncycastle.cert.ocsp.IBasicOCSPResp;
import com.itextpdf.commons.utils.DateTimeUtil;
import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.kernel.pdf.DocumentProperties;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.signatures.LtvVerification.CertificateOption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verifies the signatures in an LTV document.
 */
public class LtvVerifier extends RootStoreVerifier {

    private static final IBouncyCastleFactory BOUNCY_CASTLE_FACTORY = BouncyCastleFactoryCreator.getFactory();

    /** The Logger instance */
    protected static final Logger LOGGER = LoggerFactory.getLogger(LtvVerifier.class);

    /** Option to specify level of verification; signing certificate only or the entire chain. */
    protected CertificateOption option = CertificateOption.SIGNING_CERTIFICATE;
    /** Verify root. */
    protected boolean verifyRootCertificate = true;

    /** A document object for the revision that is being verified. */
    protected PdfDocument document;
    /** The fields in the revision that is being verified. */
    protected PdfAcroForm acroForm;
    /** The date the revision was signed, or <code>null</code> for the highest revision. */
    protected Date signDate;
    /** The signature that covers the revision. */
    protected String signatureName;
    /** The PdfPKCS7 object for the signature. */
    protected PdfPKCS7 pkcs7;
    /** Indicates if we're working with the latest revision. */
    protected boolean latestRevision = true;
    /** The document security store for the revision that is being verified */
    protected PdfDictionary dss;
    /** Security provider to use, use null for default*/
    protected String securityProviderCode = null;
    /** The meta info */
    protected IMetaInfo metaInfo;

    private SignatureUtil sgnUtil;

    /**
     * Creates a VerificationData object for a PdfReader
     *
     * @param document The document we want to verify.
     *
     * @throws GeneralSecurityException if some problem with signature or security are occurred
     */
    public LtvVerifier(PdfDocument document) throws GeneralSecurityException {
        super(null);
        initLtvVerifier(document);
    }

    public LtvVerifier(PdfDocument document, String securityProviderCode) throws GeneralSecurityException {
        super(null);
        this.securityProviderCode = securityProviderCode;
        initLtvVerifier(document);
    }

    /**
     * Sets an extra verifier.
     *
     * @param verifier the verifier to set
     */
    public void setVerifier(CertificateVerifier verifier) {
        this.verifier = verifier;
    }

    /**
     * Sets the certificate option.
     *
     * @param    option    Either CertificateOption.SIGNING_CERTIFICATE (default) or CertificateOption.WHOLE_CHAIN
     */
    public void setCertificateOption(CertificateOption option) {
        this.option = option;
    }

    /**
     * Set the verifyRootCertificate to false if you can't verify the root certificate.
     *
     * @param verifyRootCertificate false if you can't verify the root certificate, otherwise true
     */
    public void setVerifyRootCertificate(boolean verifyRootCertificate) {
        this.verifyRootCertificate = verifyRootCertificate;
    }

    /**
     * Sets the {@link IMetaInfo} that will be used during {@link PdfDocument} creation.
     *
     * @param metaInfo meta info to set
     */
    public void setEventCountingMetaInfo(IMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * Verifies all the document-level timestamps and all the signatures in the document.
     *
     * @param result a list of {@link VerificationOK} objects
     *
     * @return a list of all {@link VerificationOK} objects after verification
     *
     * @throws IOException              signals that an I/O exception has occurred
     * @throws GeneralSecurityException if some problems with signature or security occurred
     */
    public List<VerificationOK> verify(List<VerificationOK> result) throws IOException, GeneralSecurityException {
        if (result == null) {
            result = new ArrayList<>();
        }
        while (pkcs7 != null) {
            result.addAll(verifySignature());
        }
        return result;
    }

    /**
     * Verifies a document level timestamp.
     *
     * @return a list of {@link VerificationOK} objects
     *
     * @throws GeneralSecurityException if some problems with signature or security occurred
     * @throws IOException              signals that an I/O exception has occurred
     */
    public List<VerificationOK> verifySignature() throws GeneralSecurityException, IOException {
        LOGGER.info("Verifying signature.");
        List<VerificationOK> result = new ArrayList<>();
        // Get the certificate chain
        Certificate[] chain = pkcs7.getSignCertificateChain();
        verifyChain(chain);
        // how many certificates in the chain do we need to check?
        int total = 1;
        if (CertificateOption.WHOLE_CHAIN.equals(option)) {
            total = chain.length;
        }
        // loop over the certificates
        X509Certificate signCert;
        X509Certificate issuerCert;
        for (int i = 0; i < total; ) {
            // the certificate to check
            signCert = (X509Certificate) chain[i++];
            // its issuer
            issuerCert = (X509Certificate) null;
            if (i < chain.length) {
                issuerCert = (X509Certificate) chain[i];
            }
            // now lets verify the certificate
            LOGGER.info(BOUNCY_CASTLE_FACTORY.createX500Name(signCert).toString());
            List<VerificationOK> list = verify(signCert, issuerCert, signDate);
            if (list.size() == 0) {
                try {
                    signCert.verify(signCert.getPublicKey());
                    if (latestRevision && chain.length > 1) {
                        list.add(new VerificationOK(signCert, this.getClass(), "Root certificate in final revision"));
                    }
                    if (list.size() == 0 && verifyRootCertificate) {
                        throw new GeneralSecurityException();
                    } else if (chain.length > 1) {
                        list.add(new VerificationOK(signCert, this.getClass(),
                                "Root certificate passed without checking"));
                    }
                } catch (GeneralSecurityException e) {
                    throw new VerificationException(signCert, "Couldn't verify with CRL or OCSP or trusted anchor");
                }
            }
            result.addAll(list);
        }
        // go to the previous revision
        switchToPreviousRevision();
        return result;
    }

    /**
     * Checks the certificates in a certificate chain:
     * are they valid on a specific date, and
     * do they chain up correctly?
     *
     * @param chain the certificate chain
     *
     * @throws GeneralSecurityException when requested cryptographic algorithm or security provider
     *                                  is not available, if the certificate is invalid on a specific date and if the
     *                                  certificates chained up incorrectly
     */
    public void verifyChain(Certificate[] chain) throws GeneralSecurityException {
        // Loop over the certificates in the chain
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = (X509Certificate) chain[i];
            // check if the certificate was/is valid
            cert.checkValidity(signDate);
            // check if the previous certificate was issued by this certificate
            if (i > 0) {
                chain[i - 1].verify(chain[i].getPublicKey());
            }
        }
        LOGGER.info("All certificates are valid on " + signDate.toString());
    }

    /**
     * Verifies certificates against a list of CRLs and OCSP responses.
     *
     * @param signCert   the signing certificate
     * @param issuerCert the issuer's certificate
     *
     * @return a list of <code>VerificationOK</code> objects.
     * The list will be empty if the certificate couldn't be verified.
     *
     * @throws GeneralSecurityException if some problems with signature or security occurred
     * @see com.itextpdf.signatures.RootStoreVerifier#verify(java.security.cert.X509Certificate,
     *         java.security.cert.X509Certificate, java.util.Date)
     */
    public List<VerificationOK> verify(X509Certificate signCert, X509Certificate issuerCert, Date signDate)
            throws GeneralSecurityException {
        // we'll verify against the rootstore (if present)
        RootStoreVerifier rootStoreVerifier = new RootStoreVerifier(verifier);
        rootStoreVerifier.setRootStore(rootStore);
        // We'll verify against a list of CRLs
        CRLVerifier crlVerifier = new CRLVerifier(rootStoreVerifier, getCRLsFromDSS());
        crlVerifier.setRootStore(rootStore);
        crlVerifier.setOnlineCheckingAllowed(latestRevision || onlineCheckingAllowed);
        // We'll verify against a list of OCSPs
        OCSPVerifier ocspVerifier = new OCSPVerifier(crlVerifier, getOCSPResponsesFromDSS());
        ocspVerifier.setRootStore(rootStore);
        ocspVerifier.setOnlineCheckingAllowed(latestRevision || onlineCheckingAllowed);
        // We verify the chain
        return ocspVerifier.verify(signCert, issuerCert, signDate);
    }

    /**
     * Switches to the previous revision.
     *
     * @throws IOException              signals that an I/O exception has occurred
     * @throws GeneralSecurityException if some problems with signature or security occurred
     */
    public void switchToPreviousRevision() throws IOException, GeneralSecurityException {
        LOGGER.info("Switching to previous revision.");
        latestRevision = false;
        dss = document.getCatalog().getPdfObject().getAsDictionary(PdfName.DSS);
        Calendar cal = pkcs7.getTimeStampDate();
        if (cal == TimestampConstants.UNDEFINED_TIMESTAMP_DATE) {
            cal = pkcs7.getSignDate();
        }
        signDate = cal.getTime();
        List<String> names = sgnUtil.getSignatureNames();
        if (names.size() > 1) {
            signatureName = names.get(names.size() - 2);
            try (PdfReader readerTmp = new PdfReader(sgnUtil.extractRevision(signatureName))) {
                document = new PdfDocument(readerTmp, new DocumentProperties().setEventCountingMetaInfo(metaInfo));
                this.acroForm = PdfAcroForm.getAcroForm(document, true);
                this.sgnUtil = new SignatureUtil(document);
                names = sgnUtil.getSignatureNames();
                signatureName = names.get(names.size() - 1);
                pkcs7 = coversWholeDocument();
                LOGGER.info(
                        MessageFormatUtil.format("Checking {0}signature {1}", pkcs7.isTsp()
                                ? "document-level timestamp "
                                : "", signatureName));
            }
        } else {
            LOGGER.info("No signatures in revision");
            pkcs7 = null;
        }
    }

    /**
     * Gets a list of X509CRL objects from a Document Security Store.
     *
     * @return a list of CRLs
     * 
     * @throws GeneralSecurityException when requested cryptographic algorithm or security provider
     *                                  is not available
     */
    public List<X509CRL> getCRLsFromDSS() throws GeneralSecurityException {
        List<X509CRL> crls = new ArrayList<>();
        if (dss == null) {
            return crls;
        }
        PdfArray crlarray = dss.getAsArray(PdfName.CRLs);
        if (crlarray == null) {
            return crls;
        }
        for (int i = 0; i < crlarray.size(); i++) {
            PdfStream stream = crlarray.getAsStream(i);
            crls.add((X509CRL) SignUtils.parseCrlFromStream(new ByteArrayInputStream(stream.getBytes())));
        }
        return crls;
    }

    /**
     * Gets OCSP responses from the Document Security Store.
     *
     * @return a list of IBasicOCSPResp objects
     *
     * @throws GeneralSecurityException if OCSP response failed
     */
    public List<IBasicOCSPResp> getOCSPResponsesFromDSS() throws GeneralSecurityException {
        List<IBasicOCSPResp> ocsps = new ArrayList<>();
        if (dss == null) {
            return ocsps;
        }
        PdfArray ocsparray = dss.getAsArray(PdfName.OCSPs);
        if (ocsparray == null) {
            return ocsps;
        }
        for (int i = 0; i < ocsparray.size(); i++) {
            PdfStream stream = ocsparray.getAsStream(i);
            IOCSPResp ocspResponse;
            try {
                ocspResponse = BOUNCY_CASTLE_FACTORY.createOCSPResp(stream.getBytes());
            } catch (IOException e) {
                throw new GeneralSecurityException(e.getMessage());
            }
            if (ocspResponse.getStatus() == 0) {
                try {
                    ocsps.add(BOUNCY_CASTLE_FACTORY.createBasicOCSPResp(ocspResponse.getResponseObject()));
                } catch (AbstractOCSPException e) {
                    throw new GeneralSecurityException(e.toString());
                }
            }
        }
        return ocsps;
    }

    protected void initLtvVerifier(PdfDocument document) throws GeneralSecurityException {
        this.document = document;
        this.acroForm = PdfAcroForm.getAcroForm(document, true);
        this.sgnUtil = new SignatureUtil(document);
        List<String> names = sgnUtil.getSignatureNames();
        signatureName = names.get(names.size() - 1);
        this.signDate = DateTimeUtil.getCurrentTimeDate();
        pkcs7 = coversWholeDocument();
        LOGGER.info(
                MessageFormatUtil.format(
                        "Checking {0}signature {1}", pkcs7.isTsp()
                                ? "document-level timestamp "
                                : "",
                        signatureName));
    }

    /**
     * Checks if the signature covers the whole document
     * and throws an exception if the document was altered
     *
     * @return a PdfPKCS7 object
     *
     * @throws GeneralSecurityException if some problems with signature or security occurred
     */
    protected PdfPKCS7 coversWholeDocument() throws GeneralSecurityException {
        PdfPKCS7 pkcs7 = sgnUtil.readSignatureData(signatureName, securityProviderCode);
        if (sgnUtil.signatureCoversWholeDocument(signatureName)) {
            LOGGER.info("The timestamp covers whole document.");
        } else {
            throw new VerificationException((Certificate) null, "Signature doesn't cover whole document.");
        }
        if (pkcs7.verifySignatureIntegrityAndAuthenticity()) {
            LOGGER.info("The signed document has not been modified.");
            return pkcs7;
        } else {
            throw new VerificationException((Certificate) null,
                    "The document was altered after the final signature was applied.");
        }
    }
}
