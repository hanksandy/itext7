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
package com.itextpdf.kernel.pdf;

import com.itextpdf.kernel.pdf.colorspace.PdfCieBasedCs;

import java.io.InputStream;

/**
 * Specify the colour characteristics of output devices on which the document might be rendered
 * See ISO 32000-1 14.11.5: Output Intents.
 */
public class PdfOutputIntent extends PdfObjectWrapper<PdfDictionary> {


    /**
     * Creates output intent dictionary. Null values are allowed to
     * suppress any key.
     * By default output intent subtype is GTS_PDFA1, use setter to change it.
     *
     * @param outputConditionIdentifier (required) identifying the intended output device or production condition in
     *                                  human or machine readable form
     * @param outputCondition           (optional) identifying the intended output device or production
     *                                  condition in human-readable form
     * @param registryName              identifying the registry in which the condition designated by
     *                                  {@code outputConditionIdentifier} is defined
     * @param info                      (required if {@code outputConditionIdentifier} does not specify a standard
     *                                  production condition; optional otherwise) containing additional information or
     *                                  comments about the intended target device or production condition
     * @param iccStream                 ICC profile stream. User is responsible for closing the stream
     */
    public PdfOutputIntent(String outputConditionIdentifier, String outputCondition, String registryName, String info, InputStream iccStream) {
        super(new PdfDictionary());
        setOutputIntentSubtype(PdfName.GTS_PDFA1);
        getPdfObject().put(PdfName.Type, PdfName.OutputIntent);
        if (outputCondition != null)
            setOutputCondition(outputCondition);
        if (outputConditionIdentifier != null)
            setOutputConditionIdentifier(outputConditionIdentifier);
        if (registryName != null)
            setRegistryName(registryName);
        if (info != null)
            setInfo(info);
        if (iccStream != null) {
            setDestOutputProfile(iccStream);
        }
    }

    public PdfOutputIntent(PdfDictionary outputIntentDict) {
        super(outputIntentDict);
    }

    public PdfStream getDestOutputProfile() {
        return getPdfObject().getAsStream(PdfName.DestOutputProfile);
    }

    public void setDestOutputProfile(InputStream iccStream) {
        PdfStream stream = PdfCieBasedCs.IccBased.getIccProfileStream(iccStream);
        getPdfObject().put(PdfName.DestOutputProfile, stream);
    }

    public PdfString getInfo() {
        return getPdfObject().getAsString(PdfName.Info);
    }

    public void setInfo(String info) {
        getPdfObject().put(PdfName.Info, new PdfString(info));
    }

    public PdfString getRegistryName() {
        return getPdfObject().getAsString(PdfName.RegistryName);
    }

    public void setRegistryName(String registryName) {
        getPdfObject().put(PdfName.RegistryName, new PdfString(registryName));
    }

    public PdfString getOutputConditionIdentifier() {
        return getPdfObject().getAsString(PdfName.OutputConditionIdentifier);
    }

    public void setOutputConditionIdentifier(String outputConditionIdentifier) {
        getPdfObject().put(PdfName.OutputConditionIdentifier, new PdfString(outputConditionIdentifier));
    }

    public PdfString getOutputCondition() {
        return getPdfObject().getAsString(PdfName.OutputCondition);
    }

    public void setOutputCondition(String outputCondition) {
        getPdfObject().put(PdfName.OutputCondition, new PdfString(outputCondition));
    }

    public PdfName getOutputIntentSubtype() {
        return getPdfObject().getAsName(PdfName.S);
    }

    public void setOutputIntentSubtype(PdfName subtype) {
        getPdfObject().put(PdfName.S, subtype);
    }

    @Override
    protected boolean isWrappedObjectMustBeIndirect() {
        return false;
    }

}
