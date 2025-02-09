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

import com.itextpdf.io.logs.IoLogMessageConstant;
import com.itextpdf.kernel.utils.ICopyFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public abstract class PdfPrimitiveObject extends PdfObject {

	
    protected byte[] content = null;
    protected boolean directOnly;

    protected PdfPrimitiveObject() {
        super();
    }

    protected PdfPrimitiveObject(boolean directOnly) {
        super();
        this.directOnly = directOnly;
    }

    /**
     * Initialize PdfPrimitiveObject from the passed bytes.
     *
     * @param content byte content, shall not be null.
     */
    protected PdfPrimitiveObject(byte[] content) {
        this();
        assert content != null;
        this.content = content;
    }

    protected final byte[] getInternalContent() {
        if (content == null)
            generateContent();
        return content;
    }

    protected boolean hasContent() {
        return content != null;
    }

    protected abstract void generateContent();

    @Override
    public PdfObject makeIndirect(PdfDocument document, PdfIndirectReference reference) {
        if (!directOnly) {
            return super.makeIndirect(document, reference);
        } else {
            Logger logger = LoggerFactory.getLogger(PdfObject.class);
            logger.warn(IoLogMessageConstant.DIRECTONLY_OBJECT_CANNOT_BE_INDIRECT);
        }
        return this;
    }

    @Override
    public PdfObject setIndirectReference(PdfIndirectReference indirectReference) {
        if (!directOnly) {
            super.setIndirectReference(indirectReference);
        } else {
            Logger logger = LoggerFactory.getLogger(PdfObject.class);
            logger.warn(IoLogMessageConstant.DIRECTONLY_OBJECT_CANNOT_BE_INDIRECT);
        }
        return this;
    }

    @Override
    protected void copyContent(PdfObject from, PdfDocument document, ICopyFilter copyFilter) {
        super.copyContent(from, document, copyFilter);
        PdfPrimitiveObject object = (PdfPrimitiveObject) from;
        if (object.content != null)
            content = Arrays.copyOf(object.content, object.content.length);
    }

    protected int compareContent(PdfPrimitiveObject o) {
        for (int i = 0; i < Math.min(content.length, o.content.length); i++) {
            if (content[i] > o.content[i])
                return 1;
            if (content[i] < o.content[i])
                return -1;
        }
        return Integer.compare(content.length, o.content.length);
    }
}
