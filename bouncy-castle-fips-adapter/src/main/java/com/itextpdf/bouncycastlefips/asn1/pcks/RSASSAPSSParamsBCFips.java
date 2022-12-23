/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.bouncycastlefips.asn1.pcks;

import com.itextpdf.bouncycastlefips.asn1.ASN1EncodableBCFips;
import com.itextpdf.bouncycastlefips.asn1.x509.AlgorithmIdentifierBCFips;
import com.itextpdf.commons.bouncycastle.asn1.pkcs.IRSASSAPSSParams;
import com.itextpdf.commons.bouncycastle.asn1.x509.IAlgorithmIdentifier;

import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import java.math.BigInteger;

/**
 * BC-FIPS wrapper implementation for {@link IRSASSAPSSParams}.
 */
public class RSASSAPSSParamsBCFips extends ASN1EncodableBCFips implements IRSASSAPSSParams {

    /**
     * Creates new wrapper instance for {@link RSASSAPSSparams}.
     *
     * @param params {@link RSASSAPSSparams} to be wrapped
     */
    public RSASSAPSSParamsBCFips(RSASSAPSSparams params) {
        super(params);
    }


    /**
     * Gets actual org.bouncycastle object being wrapped.
     *
     * @return wrapped {@link RSASSAPSSparams}.
     */
    public RSASSAPSSparams getRSASSAPSSparams() {
        return (RSASSAPSSparams) super.getEncodable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAlgorithmIdentifier getHashAlgorithm() {
        return new AlgorithmIdentifierBCFips(getRSASSAPSSparams().getHashAlgorithm());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAlgorithmIdentifier getMaskGenAlgorithm() {
        return new AlgorithmIdentifierBCFips( getRSASSAPSSparams().getMaskGenAlgorithm());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger getSaltLength() {
        return getRSASSAPSSparams().getSaltLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigInteger getTrailerField() {
        return getRSASSAPSSparams().getTrailerField();
    }
}