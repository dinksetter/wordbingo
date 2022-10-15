package com.inksetter.bingo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;

public class WordListSource implements WordSource {
    private final List<String> wordList = new ArrayList<String>();

    public Iterator<String> currentSpot;
    
    public WordListSource(BufferedReader in) throws IOException {
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            
            StringTokenizer parser = new StringTokenizer(line);
            while (parser.hasMoreTokens()) {
                String word = parser.nextToken();
                wordList.add(word);
            }
        }
    }
    
    public WordListSource(InputStream is) throws IOException {
        this(new BufferedReader(new InputStreamReader(is)));
    }
    
    public WordListSource(String data) throws IOException {
        this(new BufferedReader(new StringReader(data)));
    }

    @Override
    synchronized
    public String nextWord() {
        if (currentSpot == null || !currentSpot.hasNext()) {
            Collections.shuffle(wordList);
            currentSpot = wordList.iterator();
        }
        return currentSpot.next();
    }

    @Override
    public Collection<String> allWords() {
        return new HashSet<>(wordList);
    }
}
