package com.inksetter.bingo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name="wordbingo")
public class GenerateBoard implements Runnable {

    @Option(names={"-s", "--size"}, description = "Size of the board in inches")
    private float boardWidth = 4.75f;

    @Option(names={"-2", "--two-up"}, description = "Two puzzles on one page")
    private boolean twoUp = false;

    @Option(names={"-F", "--free"}, description = "omit free space")
    private boolean omitFreeSpace = false;

    @Option(names={"-f", "--file"}, description = "word file")
    private String wordFile;

    @Option(names={"-h", "--header"}, description="Puzzle Heading (determines puzzle size) (default: ${DEFAULT-VALUE})", defaultValue = "BINGO")
    private String headerText;

    @Option(names={"-o", "--output"}, description = "Output file (default: ${DEFAULT-VALUE})", defaultValue = "bingo.pdf")
    private String outputFile;

    @Option(names = "--help", usageHelp = true, description = "display this help and exit")
    private boolean help;

    @Option(names={"-w", "--words"}, description = "Words to use (separated by whitespace)")
    private String wordList;

    @Option(names={"-C", "--common"}, description = "use a common word list (abclower, abcupper, kdg, sight, silente)")
    private String wordResource;

    @Option(names={"-c", "--circle"}, description = "Add circles to each item")
    private boolean useCircle = false;

    @Option(names={"-p", "--pages"}, description = "boards to generate")
    private int boardCount = 8;

    public static void main(String[] args)  {
        new CommandLine(new GenerateBoard()).execute(args);
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
            } else {
                wordReader = new WordListSource(System.in);
            }

            BoardCreator creator = new BoardCreator(boardWidth, wordReader, !omitFreeSpace, twoUp, headerText, useCircle);
            for (int i = 0; i < boardCount; i++) {
                creator.createBoard();
            }
            try (OutputStream output = new FileOutputStream(outputFile == null ? "bingo.pdf" : outputFile)) {
                creator.saveAndClose(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
