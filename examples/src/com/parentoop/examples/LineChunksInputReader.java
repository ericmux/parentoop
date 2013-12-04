package com.parentoop.examples;

import com.parentoop.core.api.InputReader;
import com.parentoop.core.data.Yielder;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class LineChunksInputReader implements InputReader {

    public static final int LINES_PER_CHUNK = 100;

    @Override
    public void read(Path file, Yielder<Serializable> chunkYielder) {
        try {
            Scanner scanner = new Scanner(Files.newInputStream(file));
            while (scanner.hasNext()) {
                StringBuilder chunkBuilder = new StringBuilder();
                int lines = LINES_PER_CHUNK;
                while (lines --> 0 && scanner.hasNext()) {
                    chunkBuilder.append('\n');
                    chunkBuilder.append(scanner.nextLine());
                }
                chunkYielder.yield(chunkBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
