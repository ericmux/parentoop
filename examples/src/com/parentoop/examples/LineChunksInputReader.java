package com.parentoop.examples;

import com.parentoop.core.api.InputReader;
import com.parentoop.core.data.Yielder;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class LineChunksInputReader implements InputReader {

    @Override
    public void read(Path file, Yielder<Serializable> chunkYielder) {
        try {
            Scanner scanner = new Scanner(Files.newInputStream(file));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                chunkYielder.yield(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
