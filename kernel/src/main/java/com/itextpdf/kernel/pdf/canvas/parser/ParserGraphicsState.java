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
package com.itextpdf.kernel.pdf.canvas.parser;


import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.Path;
import com.itextpdf.kernel.geom.ShapeTransformUtil;
import com.itextpdf.kernel.pdf.canvas.CanvasGraphicsState;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants.FillingRule;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.ClipperBridge;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.DefaultClipper;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.IClipper;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.PolyTree;

/**
 * Internal class which is essentially a {@link CanvasGraphicsState} which supports tracking of
 * clipping path state and changes.
 */
public class ParserGraphicsState extends CanvasGraphicsState {
    // NOTE: From the spec default value of this field should be the boundary of the entire imageable portion of the output page.
    private Path clippingPath;

    /**
     * Internal empty and default constructor.
     */
    ParserGraphicsState() {

    }

    /**
     * Copy constructor.
     *
     * @param source the Graphics State to copy from
     */
    ParserGraphicsState(ParserGraphicsState source) {
        super(source);

        if (source.clippingPath != null) {
            clippingPath = new Path(source.clippingPath);
        }
    }

    @Override
    public void updateCtm(Matrix newCtm) {
        super.updateCtm(newCtm);
        if (clippingPath != null) {
            transformClippingPath(newCtm);
        }
    }

    /**
     * Intersects the current clipping path with the given path.
     *
     * <strong>Note:</strong> Coordinates of the given path should be in
     * the transformed user space.
     *
     * @param path        The path to be intersected with the current clipping path.
     * @param fillingRule The filling rule which should be applied to the given path.
     *                    It should be either {@link FillingRule#EVEN_ODD} or
     *                    {@link FillingRule#NONZERO_WINDING}
     */
    public void clip(Path path, int fillingRule) {
        if (clippingPath == null || clippingPath.isEmpty()) {
            return;
        }

        Path pathCopy = new Path(path);
        pathCopy.closeAllSubpaths();

        IClipper clipper = new DefaultClipper();
        ClipperBridge.addPath(clipper, clippingPath, IClipper.PolyType.SUBJECT);
        ClipperBridge.addPath(clipper, pathCopy, IClipper.PolyType.CLIP);

        PolyTree resultTree = new PolyTree();
        clipper.execute(IClipper.ClipType.INTERSECTION, resultTree, IClipper.PolyFillType.NON_ZERO, ClipperBridge.getFillType(fillingRule));

        clippingPath = ClipperBridge.convertToPath(resultTree);
    }

    /**
     * Getter for the current clipping path.
     *
     * <strong>Note:</strong> The returned clipping path is in the transformed user space, so
     * if you want to get it in default user space, apply transformation matrix ({@link CanvasGraphicsState#getCtm()}).
     *
     * @return The current clipping path.
     */
    public Path getClippingPath() {
        return clippingPath;
    }

    /**
     * Sets the current clipping path to the specified path.
     *
     * <strong>Note:</strong>This method doesn't modify existing clipping path,
     * it simply replaces it with the new one instead.
     *
     * @param clippingPath New clipping path.
     */
    public void setClippingPath(Path clippingPath) {
        Path pathCopy = new Path(clippingPath);
        pathCopy.closeAllSubpaths();
        this.clippingPath = pathCopy;
    }

    private void transformClippingPath(Matrix newCtm) {
        clippingPath = ShapeTransformUtil.transformPath(clippingPath, newCtm);
    }
}
