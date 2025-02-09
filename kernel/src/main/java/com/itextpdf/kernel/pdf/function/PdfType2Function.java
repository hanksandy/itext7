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
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;

/**
 * This class represents Pdf type 2 function that defines an exponential
 * interpolation of one input value to n output values.
 *
 * <p>
 * For more info see ISO 32000-1, section 7.10.3 "Type 2 (Exponential Interpolation) Functions".
 */
public class PdfType2Function extends AbstractPdfFunction<PdfDictionary> {
    private double[] c0;

    private double[] c1;

    private double n;

    /**
     * Instantiates a new PdfType2Function instance based on passed PdfDictionary instance.
     *
     * @param dict the function dictionary
     */
    public PdfType2Function(PdfDictionary dict) {
        super(dict);

        final PdfNumber nObj = dict.getAsNumber(PdfName.N);
        if (nObj == null) {
            throw new PdfException(KernelExceptionMessageConstant.INVALID_TYPE_2_FUNCTION_N);
        }
        n = nObj.doubleValue();

        if ( super.getDomain().length < 2) {
            throw new PdfException(KernelExceptionMessageConstant.INVALID_TYPE_2_FUNCTION_DOMAIN);
        }

        if (n != Math.floor(n) && super.getDomain()[0] < 0) {
            throw new PdfException(KernelExceptionMessageConstant.INVALID_TYPE_2_FUNCTION_N_NOT_INTEGER);
        }
        if (n < 0 && super.clipInput(new double[] {0})[0] == 0) {
            throw new PdfException(KernelExceptionMessageConstant.INVALID_TYPE_2_FUNCTION_N_NEGATIVE);
        }

        final PdfArray c0Obj = dict.getAsArray(PdfName.C0);
        final PdfArray c1Obj = dict.getAsArray(PdfName.C1);
        final PdfArray rangeObj = dict.getAsArray(PdfName.Range);
        c0 = initializeCArray(c0Obj, c1Obj, rangeObj, 0);
        c1 = initializeCArray(c1Obj, c0Obj, rangeObj, 1);

        if (c0.length != c1.length || (super.getRange() != null && c0.length != super.getRange().length / 2)) {
            throw new PdfException(KernelExceptionMessageConstant.INVALID_TYPE_2_FUNCTION_OUTPUT_SIZE);
        }
    }

    public PdfType2Function(double[] domain, double[] range, double[] c0, double[] c1, double n) {
        super(new PdfDictionary(), PdfFunctionFactory.FUNCTION_TYPE_2, domain, range);
        setC0(c0);
        setC1(c1);
        setN(n);
    }

    public PdfType2Function(float[] domain, float[] range, float[] c0, float[] c1, double n) {
        this(convertFloatArrayToDoubleArray(domain), convertFloatArrayToDoubleArray(range),
                convertFloatArrayToDoubleArray(c0), convertFloatArrayToDoubleArray(c1), n);
    }

    @Override
    public double[] calculate(double[] input) {
        if (input == null || input.length != 1) {
            throw new PdfException(KernelExceptionMessageConstant.INVALID_INPUT_FOR_TYPE_2_FUNCTION);
        }
        double[] clipped = clipInput(input);
        final double x = clipped[0];
        final int outputSize = getOutputSize();
        final double[] output = new double[outputSize];
        for (int i = 0; i < outputSize; i++) {
            output[i] = c0[i] + Math.pow(x, n) * (c1[i] - c0[i]);
        }

        return clipOutput(output);
    }

    /**
     * Gets output size of function.
     *
     * <p>
     * If Range field is absent, the size of C0 array will be returned.
     *
     * @return output size of function
     */
    @Override
    public final int getOutputSize() {
        return getRange() == null ? c0.length : (getRange().length / 2);
    }

    /**
     * Gets values of C0 array.
     *
     * @return the values of C0 array
     */
    public final double[] getC0() {
        return c0;
    }

    /**
     * Sets values of C0 array.
     *
     * @param value  the values of C0 array
     */
    public final void setC0(double[] value) {
        getPdfObject().put(PdfName.C0, new PdfArray(value));
        c0 = value;
    }

    /**
     * Gets values of C1 array.
     *
     * @return the values of C1 array
     */
    public final double[] getC1() {
        return c1;
    }

    /**
     * Sets values of C1 array.
     *
     * @param value  the values of C1 array
     */
    public final void setC1(double[] value) {
        getPdfObject().put(PdfName.C1, new PdfArray(value));
        c1 = value;
    }

    /**
     * Gets value of N field.
     *
     * @return the value of N field
     */
    public final double getN() {
        return n;
    }

    /**
     * sets value of N field.
     *
     * @param value the value of N field
     */
    public final void setN(double value) {
        getPdfObject().put(PdfName.N, new PdfNumber(value));
        n = value;
    }

    private static double[] initializeCArray(PdfArray c, PdfArray otherC, PdfArray range, double defaultValue) {
        if (c != null) {
            return c.toDoubleArray();
        }

        double[] result;
        if (otherC == null) {
            if (range == null) {
                result = new double[1];
            } else {
                result = new double[range.size() / 2];
            }
        } else {
            result = new double[otherC.size()];
        }

        for (int i = 0; i < result.length; i++) {
            result[i] = defaultValue;
        }
        return result;
    }
}
