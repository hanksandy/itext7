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
package com.itextpdf.kernel.pdf.canvas.parser.listener;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class RegexBasedLocationExtractionStrategyTest extends ExtendedITextTest {

    private static final String sourceFolder = "./src/test/resources/com/itextpdf/kernel/parser/RegexBasedLocationExtractionStrategyTest/";

    @Test
    public void test01() throws IOException {
        System.out.println(new File(sourceFolder).getAbsolutePath());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "in01.pdf"));

        // build strategy
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy(Pattern.compile("\\{\\{Signature\\}\\}"));

        // get locations
        List<IPdfTextLocation> locationList = new ArrayList<>();
        for (int x = 1; x <= pdfDocument.getNumberOfPages(); x++) {
            new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(x));
            for(IPdfTextLocation location : extractionStrategy.getResultantLocations()) {
                if(location != null) {
                    locationList.add(location);
                }
            }
        }

        // compare
        Assert.assertEquals(1, locationList.size());

        IPdfTextLocation loc = locationList.get(0);

        Assert.assertEquals("{{Signature}}", loc.getText());
        Assert.assertEquals(23, (int) loc.getRectangle().getX());
        Assert.assertEquals(375, (int) loc.getRectangle().getY());
        Assert.assertEquals(55, (int) loc.getRectangle().getWidth());
        Assert.assertEquals(11, (int) loc.getRectangle().getHeight());

        // close
        pdfDocument.close();
    }


    // https://jira.itextsupport.com/browse/DEVSIX-1940
    // text is 'calligraphy' and 'll' is composing a ligature

    @Test
    public void testLigatureBeforeLigature() throws IOException {
        System.out.println(new File(sourceFolder).getAbsolutePath());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "ligature.pdf"));

        // build strategy
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("ca");

        // get locations
        List<IPdfTextLocation> locationList = new ArrayList<>();
        for (int x = 1; x <= pdfDocument.getNumberOfPages(); x++) {
            new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(x));
            for(IPdfTextLocation location : extractionStrategy.getResultantLocations()) {
                if(location != null) {
                    locationList.add(location);
                }
            }
        }

        // compare
        Assert.assertEquals(1, locationList.size());

        IPdfTextLocation loc = locationList.get(0);

        Assert.assertEquals("ca", loc.getText());
        Rectangle rect = loc.getRectangle();
        Assert.assertEquals(36, rect.getX(), 0.0001);
        Assert.assertEquals(655.4600, rect.getY(), 0.0001);
        Assert.assertEquals(25.1000, rect.getWidth(), 0.0001);
        Assert.assertEquals(20, rect.getHeight(), 0.0001);

        pdfDocument.close();
    }

    @Test
    public void testLigatureCrossLigature() throws IOException {
        System.out.println(new File(sourceFolder).getAbsolutePath());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "ligature.pdf"));

        // build strategy
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("al");

        // get locations
        List<IPdfTextLocation> locationList = new ArrayList<>();
        for (int x = 1; x <= pdfDocument.getNumberOfPages(); x++) {
            new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(x));
            for(IPdfTextLocation location : extractionStrategy.getResultantLocations()) {
                if(location != null) {
                    locationList.add(location);
                }
            }
        }

        // compare
        Assert.assertEquals(1, locationList.size());

        IPdfTextLocation loc = locationList.get(0);

        Assert.assertEquals("al", loc.getText());
        Rectangle rect = loc.getRectangle();
        Assert.assertEquals(48.7600, rect.getX(), 0.0001);
        Assert.assertEquals(655.4600, rect.getY(), 0.0001);
        Assert.assertEquals(25.9799, rect.getWidth(), 0.0001);
        Assert.assertEquals(20, rect.getHeight(), 0.0001);

        pdfDocument.close();
    }

    @Test
    public void testLigatureInLigature() throws IOException {
        System.out.println(new File(sourceFolder).getAbsolutePath());

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "ligature.pdf"));

        // build strategy
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("l");

        // get locations
        List<IPdfTextLocation> locationList = new ArrayList<>();
        for (int x = 1; x <= pdfDocument.getNumberOfPages(); x++) {
            new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(x));
            for(IPdfTextLocation location : extractionStrategy.getResultantLocations()) {
                if(location != null) {
                    locationList.add(location);
                }
            }
        }

        // compare
        Assert.assertEquals(1, locationList.size());

        IPdfTextLocation loc = locationList.get(0);

        Assert.assertEquals("l", loc.getText());
        Rectangle rect = loc.getRectangle();
        Assert.assertEquals(61.0999, rect.getX(), 0.0001);
        Assert.assertEquals(655.4600, rect.getY(), 0.0001);
        Assert.assertEquals(13.6399, rect.getWidth(), 0.0001);
        Assert.assertEquals(20, rect.getHeight(), 0.0001);

        pdfDocument.close();
    }

    @Test
    public void testRotatedText() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "rotatedText.pdf"));

        // build strategy
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("abc");

        // get locations
        List<IPdfTextLocation> locationList = new ArrayList<>();
        for (int x = 1; x <= pdfDocument.getNumberOfPages(); x++) {
            new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(x));
            for(IPdfTextLocation location : extractionStrategy.getResultantLocations()) {
                if(location != null) {
                    locationList.add(location);
                }
            }
        }

        // compare
        Assert.assertEquals(2, locationList.size());
        Assert.assertTrue(locationList.get(0).getRectangle().equalsWithEpsilon(new Rectangle(188.512f, 450f, 14.800003f, 25.791992f)));
        Assert.assertTrue(locationList.get(1).getRectangle().equalsWithEpsilon(new Rectangle(36f, 746.688f, 25.792f, 14.799988f)));

        pdfDocument.close();
    }

    @Test
    public void regexStartedWithWhiteSpaceTest() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "regexStartedWithWhiteSpaceTest.pdf"));
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("\\sstart");
        new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(1));
        List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
        pdfDocument.close();

        Assert.assertEquals(1, locations.size());
        Assert.assertEquals(" start", locations.get(0).getText());
        Assert.assertTrue(
                new Rectangle(92.3f, 743.3970f, 20.6159f, 13.2839f).equalsWithEpsilon(locations.get(0).getRectangle()));
    }

    @Test
    public void regexStartedWithNewLineTest() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "regexStartedWithNewLineTest.pdf"));
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("\\nstart");
        new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(1));
        List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
        pdfDocument.close();

        Assert.assertEquals(1, locations.size());
        Assert.assertEquals("\nstart", locations.get(0).getText());
        Assert.assertTrue(
                new Rectangle(56.8f, 729.5970f, 20.6159f, 13.2839f).equalsWithEpsilon(locations.get(0).getRectangle()));
    }

    @Test
    public void regexWithWhiteSpacesTest() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "regexWithWhiteSpacesTest.pdf"));
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy(
                "\\sstart\\s");
        new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(1));
        List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
        pdfDocument.close();

        Assert.assertEquals(1, locations.size());
        Assert.assertEquals(" start ", locations.get(0).getText());
        Assert.assertTrue(
                new Rectangle(92.3f, 743.3970f, 20.6159f, 13.2839f).equalsWithEpsilon(locations.get(0).getRectangle()));
    }

    @Test
    public void regexWithNewLinesTest() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "regexWithNewLinesTest.pdf"));
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy(
                "\\nstart\\n");
        new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(1));
        List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
        pdfDocument.close();

        Assert.assertEquals(1, locations.size());
        Assert.assertEquals("\nstart\n", locations.get(0).getText());
        Assert.assertTrue(
                new Rectangle(56.8f, 729.5970f, 20.6159f, 13.2839f).equalsWithEpsilon(locations.get(0).getRectangle()));
    }


    @Test
    public void regexWithNewLineBetweenWordsTest() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "regexWithNewLineBetweenWordsTest.pdf"));
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy(
                "hello\\nworld");
        new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(1));
        List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
        pdfDocument.close();

        Assert.assertEquals(2, locations.size());
        Assert.assertEquals("hello\nworld", locations.get(0).getText());
        Assert.assertEquals("hello\nworld", locations.get(1).getText());
        Assert.assertTrue(
                new Rectangle(56.8f, 729.5970f, 27.8999f, 13.2839f).equalsWithEpsilon(locations.get(0).getRectangle()));
        Assert.assertTrue(
                new Rectangle(56.8f, 743.3970f, 23.9039f, 13.2839f).equalsWithEpsilon(locations.get(1).getRectangle()));
    }


    @Test
    public void regexWithOnlyNewLine() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "regexWithNewLinesTest.pdf"));
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("\\n");
        new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(1));
        List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
        pdfDocument.close();

        Assert.assertEquals(0, locations.size());
    }

    @Test
    public void regexWithOnlyWhiteSpace() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "regexWithWhiteSpacesTest.pdf"));
        RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy(" ");
        new PdfCanvasProcessor(extractionStrategy).processPageContent(pdfDocument.getPage(1));
        List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
        pdfDocument.close();

        Assert.assertEquals(0, locations.size());
    }

    @Test
    public void sortCompareTest() throws IOException {
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(sourceFolder + "sortCompare.pdf"))) {
            RegexBasedLocationExtractionStrategy extractionStrategy = new RegexBasedLocationExtractionStrategy("a");
            PdfCanvasProcessor pdfCanvasProcessor = new PdfCanvasProcessor(extractionStrategy);
            pdfCanvasProcessor.processPageContent(pdfDocument.getPage(1));
            pdfCanvasProcessor.processPageContent(pdfDocument.getPage(2));
            List<IPdfTextLocation> locations = new ArrayList<>(extractionStrategy.getResultantLocations());
            Assert.assertEquals(13, locations.size());
        }
    }
}
