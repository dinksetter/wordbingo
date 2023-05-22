package com.inksetter.bingo;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

public class BoardCreator {
    
    private final PDDocument document;
    private final PDFont textFont;
    private final PDFont headingFont;
    private final WordSource words;
    private final float xOffset;
    private final float yOffset;
    private final float boxSize;

    private final float headingSize;
    private final float textSize;
    private final float freeTextSize;
    private final boolean hasFreeSpace;
    private final boolean hasHeaders;
    private final String headerText;
    private final int boardCols;
    private final boolean twoUp;
    private final boolean useCircles;

    public BoardCreator(float boardWidth, WordSource words, boolean hasFreeSpace, boolean twoUp, String headerText, boolean useCircles) throws IOException {
        this.hasFreeSpace = hasFreeSpace;
        this.hasHeaders = (headerText != null && headerText.trim().length() != 0);
        this.headerText = headerText;
        this.twoUp = twoUp;
        this.boardCols = headerText.length();
        this.useCircles = useCircles;
        document = new PDDocument();
        
        float pageWidth = (8.5f * 72f);
        float pageHeight = (11f * 72f);

        if (twoUp) {
            pageHeight = pageHeight / 2.0f;
        }

        boxSize = (boardWidth * 72f) / ((float)boardCols);
        xOffset = (pageWidth - (boxSize * boardCols)) / 2;
        yOffset = (pageHeight - (boxSize * boardCols) - (hasHeaders ? boxSize : 0) / 2) / 2;
        
        // Create a new font object selecting one of the PDF base fonts
        textFont = PDType0Font.load(document, BoardCreator.class.getResourceAsStream("/fonts/PRINC___.TTF"));
        headingFont = PDType0Font.load(document, BoardCreator.class.getResourceAsStream("/fonts/COOPBL.TTF"));
        this.words = words;
        
        // Discover Ideal Font Size
        float maxWidth = 0f;
        for (String word : words.allWords()) {
            float textWidth = textFont.getStringWidth(word) / 1000;
            if (textWidth > maxWidth) maxWidth = textWidth;
        }
        
        // Text and free space text are determined by box width
        float idealSize = (boxSize - 6) / maxWidth; // More space (6) around text 
        if (idealSize > (boxSize - 6)) idealSize = boxSize - 6; // Make sure it's not bigger than the height of the box
        textSize = idealSize * 0.90f;

        freeTextSize = (boxSize - 4) / (headingFont.getStringWidth("FREE") / 1000); // Less space (4) around "FREE"
        
        // Heading is determined by box height.
        headingSize = hasHeaders ? (boxSize/2 - 2) : 0;
        
        System.out.println("Font Size = [" + textSize + "/" + freeTextSize + "/" + headingSize +"]");
    }
    
    public void saveAndClose(String name) throws IOException {
        document.save(name);
        document.close();
    }
    
    public void createBoard() throws IOException {
        // Start a new content stream which will "hold" the to be created content
        PDPage page = new PDPage();
        document.addPage( page );
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        
        renderPuzzle(contentStream, 0f);

        if (twoUp) {
            renderPuzzle(contentStream, (11f * 72f) / 2);
        }

        // Make sure that the content stream is closed:
        contentStream.close();
    }

    private void renderPuzzle(PDPageContentStream contentStream, float yStart) throws IOException {
        // Bold Lines
        contentStream.setLineWidth(2.0f);
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setNonStrokingColor(Color.BLACK);
        words.reset();

        // Draw boxes for each square on the board
        for (int i = 0; i < boardCols; i++) {
            for (int j = 0; j < boardCols; j++) {
                contentStream.addRect(xOffset + boxSize * i, yOffset + yStart + boxSize * j, boxSize, boxSize);
                if (useCircles) {
                    drawCircleInRect(contentStream, xOffset + boxSize * i, yOffset + yStart + boxSize * j, boxSize);
                }
            }
        }

        // If there's a header section, add a "square" for each letter. The header boxes are half-height.
        if (hasHeaders) {
            for (int i = 0; i < boardCols; i++) {
                contentStream.addRect(xOffset + boxSize * i, yOffset + yStart + boxSize * boardCols, boxSize, boxSize / 2);
            }
        }

        contentStream.stroke();

        // regular lines.
        contentStream.setLineWidth(0.25f);
        contentStream.addRect(xOffset - 9, yOffset + yStart - 9, boxSize * boardCols + 18, boxSize * boardCols + (hasHeaders ? boxSize / 2 : 0) + 18);
        contentStream.stroke();

        // Draw the heading Text
        if (hasHeaders) {
            for (int i = 0; i < boardCols; i++) {
                drawTextInBox(contentStream, headerText.substring(i, i + 1), i, boardCols, headingFont, headingSize, yStart);
            }
        }

        for (int row = 0; row < boardCols; row++) {
            for (int col = 0; col < boardCols; col++) {
                if (row == (boardCols / 2) && col == (boardCols / 2) && hasFreeSpace) {
                    // Fill in the free space first. White on black
                    contentStream.setNonStrokingColor(Color.BLACK);
                    contentStream.addRect(xOffset + boxSize * (boardCols / 2), yOffset + yStart + boxSize * (boardCols / 2), boxSize, boxSize);
                    contentStream.fill();
                    contentStream.setNonStrokingColor(Color.WHITE);
                    drawTextInBox(contentStream, "FREE", (boardCols / 2), (boardCols / 2), headingFont, freeTextSize, yStart);

                    contentStream.setNonStrokingColor(Color.BLACK);
                }
                else {
                    drawTextInBox(contentStream, words.nextWord(), col, boardCols - row - 1, textFont, textSize, yStart);
                }
            }
        }
    }

    private void drawTextInBox(PDPageContentStream contentStream, String inText, int x, int y, PDFont font, float size, float yStart) throws IOException {
        boolean isTitle = (y > boardCols - 1);
        float boxHeight = isTitle ? boxSize / 2 : boxSize;

        String text = inText.replace('_', ' ');

        // Determine the width of the string, for centering purposes.
        float textWidth = font.getStringWidth(text) / 1000 * size;
        
        // Create text in the middle of the box
        contentStream.beginText();
        contentStream.setFont(font, size);
        contentStream.newLineAtOffset(boxSize * x + xOffset + (boxSize/2 - textWidth/2), boxSize * y + yOffset + yStart + boxHeight/2 - size/3f);
        contentStream.setRenderingMode(RenderingMode.FILL_STROKE);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawCircleInRect(PDPageContentStream contentStream, float cx, float cy, float boxSize) throws IOException {
        final float k = 0.552284749831f;

        float padding = boxSize * 0.05f; // 5% padding
        cx += boxSize/2.0f;
        cy += boxSize/2.0f;

        float r = boxSize/2.0f - padding;

        contentStream.moveTo(cx - r, cy);
        contentStream.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r);
        contentStream.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
        contentStream.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r);
        contentStream.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);

        contentStream.fill();
    }
}
