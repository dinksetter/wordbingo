package com.inksetter.bingo;

import java.io.IOException;
import java.io.FileInputStream;

import picocli.CommandLine;
import picocli.CommandLine.Option;

public class GenerateBoard implements Runnable {
    private static final int BOARD_COUNT = 8;

    // Board Width in inches
    public static final float BOARD_WIDTH = 4.75f;

    @Option(names={"-s", "--size"}, description = "Size of the board in inches")
    private float boardWidth = 4.75f;

    @Option(names={"-2", "--two-up"}, description = "Two puzzles on one page")
    private boolean twoUp = false;

    @Option(names={"-F", "--free"}, description = "omit free space")
    private boolean omitFreeSpace = false;

    @Option(names={"-f", "--file"}, description = "word file")
    private String wordFile;

    @Option(names={"-h", "--header"}, description="headerText")
    private String headerText = "BINGO";


    @Option(names={"-x", "--columns"}, description = "number of columns (and rows) for the puzzle")
    private Integer columns;

    @Option(names={"-o", "--output"}, description = "Output file")
    private String outputFile;

    @Option(names={"-w", "--words"}, description = "Words to use (separated by whitespace)")
    private String wordList;

    @Option(names={"-C", "--common"}, description = "use a common word list (abclower, abcupper, kdg, sight, silente)")
    private String wordResource;

    @Option(names={"-c", "--circle"}, description = "Add circles to each item")
    private boolean useCircle = false;


    public static void main(String[] args) throws IOException {
        CommandLine.run(new GenerateBoard(), args);
    }

    public void run() {
        try {
            WordListSource wordReader;
            if (wordResource != null) {
                wordReader = new WordListSource(getClass().getResourceAsStream("/" + wordResource + ".txt"));
            } else if (wordFile != null) {
                wordReader = new WordListSource(new FileInputStream(wordFile));
            } else if (wordList != null) {
                wordReader = new WordListSource(wordList);
            }
            else {
                wordReader = new WordListSource(System.in);
            }

            BoardCreator creator = new BoardCreator(boardWidth, wordReader, !omitFreeSpace, twoUp, headerText, useCircle);
            for (int i = 0; i < BOARD_COUNT; i++) {
                creator.createBoard();
            }
            creator.saveAndClose(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
