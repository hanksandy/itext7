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
package com.itextpdf.io.font.otf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LookupType 3: Alternate Substitution Subtable
 * @author psoares
 */
public class GsubLookupType3 extends OpenTableLookup {

    private Map<Integer, int[]> substMap;

    public GsubLookupType3(OpenTypeFontTableReader openReader, int lookupFlag, int[] subTableLocations) throws java.io.IOException {
        super(openReader, lookupFlag, subTableLocations);
        substMap = new HashMap<>();
        readSubTables();
    }

    @Override
    public boolean transformOne(GlyphLine line) {
        if (line.idx >= line.end) {
            return false;
        }
        Glyph g = line.get(line.idx);
        boolean changed = false;
        if (!openReader.isSkip(g.getCode(), lookupFlag)) {
            int[] substCode = substMap.get(g.getCode());

            // there is no need to substitute a symbol with itself
            if (substCode != null && substCode[0] != g.getCode()) {
                line.substituteOneToOne(openReader, substCode[0]);
                changed = true;
            }
        }
        line.idx++;
        return changed;
    }

    @Override
    protected void readSubTable(int subTableLocation) throws java.io.IOException {
        openReader.rf.seek(subTableLocation);
        int substFormat = openReader.rf.readShort();
        assert substFormat == 1;
        int coverage = openReader.rf.readUnsignedShort();
        int alternateSetCount = openReader.rf.readUnsignedShort();
        int[][] substitute = new int[alternateSetCount][];
        int[] alternateLocations = openReader.readUShortArray(alternateSetCount, subTableLocation);
        for (int k = 0; k < alternateSetCount; k++) {
            openReader.rf.seek(alternateLocations[k]);
            int glyphCount = openReader.rf.readUnsignedShort();
            substitute[k] = openReader.readUShortArray(glyphCount);
        }
        List<Integer> coverageGlyphIds = openReader.readCoverageFormat(subTableLocation + coverage);
        for (int k = 0; k < alternateSetCount; ++k) {
            substMap.put(coverageGlyphIds.get(k), substitute[k]);
        }
    }

    @Override
    public boolean hasSubstitution(int index) {
        return substMap.containsKey(index);
    }
}

