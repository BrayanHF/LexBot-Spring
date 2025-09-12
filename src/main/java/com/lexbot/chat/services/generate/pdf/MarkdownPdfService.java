package com.lexbot.chat.services.generate.pdf;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.font.FontProvider;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class MarkdownPdfService {

    public byte[] generatePdfFromMarkdown(String markdown) {
        try {
            MutableDataSet options = new MutableDataSet();
            options.set(Parser.EXTENSIONS, java.util.Collections.emptyList());
            options.set(HtmlRenderer.SUPPRESS_HTML, false);
            options.set(HtmlRenderer.ESCAPE_HTML, false);

            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();
            Document document = parser.parse(markdown);
            String html = renderer.render(document);

            String htmlWithStyle = """
                <html>
                <head>
                  <style>
                    body {
                      font-family: sans-serif;
                      font-size: 12pt;
                      line-height: 1.5;
                      color: #000000;
                      text-align: justify;
                      margin: 40px;
                    }
                    h1, h2, h3 {
                      font-size: 12pt;
                      font-weight: bold;
                    }
                    h1 {
                      text-align: center;
                      text-transform: uppercase;
                    }
                    h2, h3 {
                      text-align: left;
                    }
                    ul, ol {
                      padding-left: 0;
                      margin-left: 0;
                      list-style-position: inside;
                    }
                  </style>
                </head>
                <body>
                """ + html + """
                </body>
                </html>
                """;

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);

            FontProvider fontProvider;
            try {
                fontProvider = new FontProvider();
                fontProvider.addFont(extractFontToTempFile("fonts/arial.ttf"));
                fontProvider.addFont(extractFontToTempFile("fonts/arialbd.ttf"));
                fontProvider.addFont(extractFontToTempFile("fonts/arialbi.ttf"));
                fontProvider.addFont(extractFontToTempFile("fonts/ariali.ttf"));
            } catch (Exception e) {
                fontProvider = new DefaultFontProvider();
                fontProvider.addStandardPdfFonts();
                fontProvider.addSystemFonts();
            }

            ConverterProperties props = new ConverterProperties();
            props.setFontProvider(fontProvider);
            props.setCharset("UTF-8");

            HtmlConverter.convertToPdf(htmlWithStyle, pdf, props);

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private String extractFontToTempFile(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new FileNotFoundException("Font not found: " + resourcePath);
            }

            File tempFile = File.createTempFile("font-", ".ttf");
            tempFile.deleteOnExit();
            try (OutputStream os = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                }
            }
            return tempFile.getAbsolutePath();
        }
    }

}
