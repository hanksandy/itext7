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
package com.itextpdf.kernel.pdf.function;

import com.itextpdf.kernel.exceptions.KernelExceptionMessageConstant;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.colorspace.PdfColorSpace;

public class PdfType4Function extends AbstractPdfFunction<PdfStream> {
    public PdfType4Function(PdfStream dict) {
        super(dict);
    }

    public PdfType4Function(double[] domain, double[] range, byte[] code) {
        super(new PdfStream(code), PdfFunctionFactory.FUNCTION_TYPE_4, domain, range);
    }

    public PdfType4Function(float[] domain, float[] range, byte[] code) {
        this(convertFloatArrayToDoubleArray(domain), convertFloatArrayToDoubleArray(range), code);
    }

    @Override
    public boolean checkCompatibilityWithColorSpace(PdfColorSpace alternateSpace) {
        return getInputSize() == 1 && getOutputSize() == alternateSpace.getNumberOfComponents();
    }

    @Override
    public double[] calculate(double[] input) {
        throw new UnsupportedOperationException(KernelExceptionMessageConstant.TYPE4_EXECUTION_NOT_SUPPORTED);
    }
}
