package com.parentoop.network.api;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileTransferHelper {

    private static final int BUFFER_SIZE = 8192;

    public static void sendFile(Path file, ObjectOutputStream outputStream) throws IOException {
        try (InputStream fileStream = new BufferedInputStream(Files.newInputStream(file))) {
            long fileSize = Files.size(file);
            outputStream.writeLong(fileSize);
            pipeStreams(fileStream, outputStream, fileSize);
        }
    }

    public static void receiveFile(Path dest, ObjectInputStream inputStream) throws IOException {
        try (OutputStream fileStream = new BufferedOutputStream(Files.newOutputStream(dest))) {
            long fileSize = inputStream.readLong();
            pipeStreams(inputStream, fileStream, fileSize);
        }
    }

    private static void pipeStreams(InputStream from, OutputStream to, long numBytes) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (numBytes > 0) {
            int bytesRead = from.read(buffer, 0, (int) Math.min(buffer.length, numBytes));
            if (bytesRead == -1) break;
            numBytes -= bytesRead;
            to.write(buffer, 0, bytesRead);
        }
        to.flush();
    }
}
