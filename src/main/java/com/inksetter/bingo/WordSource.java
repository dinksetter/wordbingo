package com.inksetter.bingo;

import java.util.Collection;

public interface WordSource {
    Collection<String> allWords();
    String nextWord();
}
