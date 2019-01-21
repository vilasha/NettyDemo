package com.nettydemo.client;

import java.util.Random;

public class MessageGenerator {
    private static String generateRandomWords(int wordCount)
    {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            char[] word = new char[random.nextInt(8)+3];
            for(int j = 0; j < word.length; j++)
                word[j] = (char)('a' + random.nextInt(26));
            sb.append(word);
            sb.append(random.nextBoolean() ? " " : random.nextBoolean() ? ", " : ". ");
        }
        return sb.toString();
    }
}
