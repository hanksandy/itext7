/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

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
package com.itextpdf.svg.renderers;

import com.itextpdf.kernel.geom.Rectangle;

import java.util.Map;

/**
 * Interface for SvgNodeRenderer, the renderer draws the SVG to its Pdf-canvas
 * passed in {@link SvgDrawContext}, applying styling
 * (CSS and attributes).
 */
public interface ISvgNodeRenderer {

    /**
     * Sets the parent of this renderer. The parent may be the source of
     * inherited properties and default values.
     *
     * @param parent the parent renderer
     */
    void setParent(ISvgNodeRenderer parent);

    /**
     * Gets the parent of this renderer. The parent may be the source of
     * inherited properties and default values.
     *
     * @return the parent renderer; null in case of a root node
     */
    ISvgNodeRenderer getParent();

    /**
     * Draws this element to a canvas-like object maintained in the context.
     *
     * @param context the object that knows the place to draw this element and
     *                maintains its state
     */
    void draw(SvgDrawContext context);

    /**
     * Sets the map of XML node attributes and CSS style properties that this
     * renderer needs.
     *
     * @param attributesAndStyles the mapping from key names to values
     */
    void setAttributesAndStyles(Map<String, String> attributesAndStyles);

    /**
     * Retrieves the property value for a given key name.
     *
     * @param key the name of the property to search for
     *
     * @return the value for this key, or {@code null}
     */
    String getAttribute(String key);

    /**
     * Sets a property key and value pairs for a given attribute
     *
     * @param key   the name of the attribute
     * @param value the value of the attribute
     */
    void setAttribute(String key, String value);

    /**
     * Get a modifiable copy of the style and attribute map
     *
     * @return copy of the attributes and styles-map
     */
    Map<String, String> getAttributeMapCopy();

    /**
     * Creates a deep copy of this renderer, including it's subtree of children
     *
     * @return deep copy of this renderer
     */
    ISvgNodeRenderer createDeepCopy();

    /**
     * Calculates the current object bounding box.
     *
     * @param context the current context, for instance it contains current viewport and available
     *                font data
     *
     * @return the {@link Rectangle} representing the current object's bounding box, or null
     * if bounding box is undefined
     */
    Rectangle getObjectBoundingBox(SvgDrawContext context);
}
