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
package com.itextpdf.forms;

import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormAnnotation;
import com.itextpdf.forms.fields.PdfTextFormField;
import com.itextpdf.forms.fields.RadioFormFieldBuilder;
import com.itextpdf.forms.fields.TextFormFieldBuilder;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.logs.KernelLogMessageConstant;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.BouncyCastleIntegrationTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Category(BouncyCastleIntegrationTest.class)
public class PdfEncryptionTest extends ExtendedITextTest {
    public static final String sourceFolder = "./src/test/resources/com/itextpdf/forms/PdfEncryptionTest/";
    public static final String destinationFolder = "./target/test/com/itextpdf/forms/PdfEncryptionTest/";

    /**
     * User password.
     */
    public static byte[] USER = "Hello".getBytes(StandardCharsets.ISO_8859_1);

    /**
     * Owner password.
     */
    public static byte[] OWNER = "World".getBytes(StandardCharsets.ISO_8859_1);

    @BeforeClass
    public static void beforeClass() {
        createOrClearDestinationFolder(destinationFolder);
    }

    // Custom entry in Info dictionary is used because standard entried are gone into metadata in PDF 2.0
    static final String customInfoEntryKey = "Custom";
    static final String customInfoEntryValue = "String";

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = KernelLogMessageConstant.MD5_IS_NOT_FIPS_COMPLIANT,
            ignore = true))
    public void encryptedDocumentWithFormFields() throws IOException {
        PdfReader reader = new PdfReader(sourceFolder + "encryptedDocumentWithFormFields.pdf",
                new ReaderProperties().setPassword("12345".getBytes(StandardCharsets.ISO_8859_1)));
        PdfDocument pdfDocument = new PdfDocument(reader);

        PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDocument, false);

        acroForm.getField("personal.name").getPdfObject();
        pdfDocument.close();
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = KernelLogMessageConstant.MD5_IS_NOT_FIPS_COMPLIANT,
            ignore = true))
    public void encryptAes256Pdf2PermissionsTest01() throws InterruptedException, IOException {
        String filename = "encryptAes256Pdf2PermissionsTest01.pdf";
        int permissions = EncryptionConstants.ALLOW_FILL_IN | EncryptionConstants.ALLOW_SCREENREADERS | EncryptionConstants.ALLOW_DEGRADED_PRINTING;
        PdfDocument pdfDoc = new PdfDocument(
                new PdfWriter(destinationFolder + filename,
                        new WriterProperties()
                                .setPdfVersion(PdfVersion.PDF_2_0)
                                .setStandardEncryption(USER, OWNER, permissions, EncryptionConstants.ENCRYPTION_AES_256)));
        pdfDoc.getDocumentInfo().setMoreInfo(customInfoEntryKey, customInfoEntryValue);
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        PdfTextFormField textField1 = new TextFormFieldBuilder(pdfDoc, "Name")
                .setWidgetRectangle(new Rectangle(100, 600, 200, 30)).createText();
        textField1.setValue("Enter your name");
        form.addField(textField1);
        PdfTextFormField textField2 = new TextFormFieldBuilder(pdfDoc, "Surname")
                .setWidgetRectangle(new Rectangle(100, 550, 200, 30)).createText();
        textField2.setValue("Enter your surname");
        form.addField(textField2);

        String sexFormFieldName = "Sex";
        RadioFormFieldBuilder builder = new RadioFormFieldBuilder(pdfDoc, sexFormFieldName);
        PdfButtonFormField group = builder.createRadioGroup();
        group.setValue("Male");
        PdfFormAnnotation radio1 = builder
                .createRadioButton( "Male",new Rectangle(100, 530, 10, 10));
        PdfFormAnnotation radio2 = builder
                .createRadioButton( "Female",  new Rectangle(120, 530, 10, 10));
        group.addKid(radio1);
        group.addKid(radio2);

        form.addField(group);

        pdfDoc.close();

        CompareTool compareTool = new CompareTool();
        String errorMessage = compareTool.compareByContent(destinationFolder + filename, sourceFolder + "cmp_" + filename, destinationFolder, "diff_", USER, USER);
        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = KernelLogMessageConstant.MD5_IS_NOT_FIPS_COMPLIANT,
            ignore = true))
    public void encryptAes256Pdf2PermissionsTest02() throws InterruptedException, IOException {
        String filename = "encryptAes256Pdf2PermissionsTest02.pdf";
        // This test differs from the previous one (encryptAes256Pdf2PermissionsTest01) only in permissions.
        // Here we do not allow to fill the form in.
        int permissions = EncryptionConstants.ALLOW_SCREENREADERS | EncryptionConstants.ALLOW_DEGRADED_PRINTING;
        PdfDocument pdfDoc = new PdfDocument(
                new PdfWriter(destinationFolder + filename,
                        new WriterProperties()
                                .setPdfVersion(PdfVersion.PDF_2_0)
                                .setStandardEncryption(USER, OWNER, permissions, EncryptionConstants.ENCRYPTION_AES_256)));
        pdfDoc.getDocumentInfo().setMoreInfo(customInfoEntryKey, customInfoEntryValue);
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        PdfTextFormField textField1 = new TextFormFieldBuilder(pdfDoc, "Name")
                .setWidgetRectangle(new Rectangle(100, 600, 200, 30)).createText();
        textField1.setValue("Enter your name");
        form.addField(textField1);
        PdfTextFormField textField2 = new TextFormFieldBuilder(pdfDoc, "Surname")
                .setWidgetRectangle(new Rectangle(100, 550, 200, 30)).createText();
        textField2.setValue("Enter your surname");
        form.addField(textField2);

        String sexFormFieldName = "Sex";
        RadioFormFieldBuilder builder = new RadioFormFieldBuilder(pdfDoc, sexFormFieldName);
        PdfButtonFormField group = builder.createRadioGroup();
        group.setValue("Male");
        PdfFormAnnotation radio1 = new RadioFormFieldBuilder(pdfDoc, sexFormFieldName)
                .createRadioButton("Male",new Rectangle(100, 530, 10, 10));
        PdfFormAnnotation radio2 = new RadioFormFieldBuilder(pdfDoc, sexFormFieldName)
                .createRadioButton( "Female",new Rectangle(120, 530, 10, 10));

        group.addKid(radio1);
        group.addKid(radio2);

        form.addField(group);

        pdfDoc.close();

        CompareTool compareTool = new CompareTool();
        String errorMessage = compareTool.compareByContent(destinationFolder + filename, sourceFolder + "cmp_" + filename, destinationFolder, "diff_", USER, USER);
        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}
