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

import java.security.GeneralSecurityException;

/**
 * Interface that needs to be implemented to do the actual signing.
 * For instance: you'll have to implement this interface if you want
 * to sign a PDF using a smart card.
 * @author Paulo Soares
 */
public interface IExternalSignature {

    /**
     * Returns the digest algorithm.
     *
     * @return	The digest algorithm (e.g. "SHA-1", "SHA-256,...").
     */
    String getDigestAlgorithmName();

    /**
     * Returns the signature algorithm used for signing, disregarding the digest function.
     *
     * @return The signature algorithm ("RSA", "DSA", "ECDSA", "Ed25519" or "Ed448").
     */
    String getSignatureAlgorithmName();

    /**
     * Return the algorithm parameters that need to be encoded together with the signature mechanism identifier.
     * If there are no parameters, return `null`.
     * A non-null value is required for RSASSA-PSS; see {@link RSASSAPSSMechanismParams}.
     *
     * @return algorithm parameters
     */
    ISignatureMechanismParams getSignatureMechanismParameters();

    /**
     * Signs the given message using the encryption algorithm in combination
     * with the hash algorithm.
     * @param message The message you want to be hashed and signed.
     * @return	A signed message digest.
     * @throws GeneralSecurityException when requested cryptographic algorithm or security provider
     * is not available
     */
    byte[] sign(byte[] message) throws GeneralSecurityException;
}
