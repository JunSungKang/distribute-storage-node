package com.jskang.storagenode.reedsolomon;

import com.backblaze.erasure.ReedSolomon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReedSolomonEncoding implements ReedSolomonCommon {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private byte[] allBytes;

    public ReedSolomonEncoding(byte[] allBytes) {
        this.allBytes = new byte[allBytes.length];
        System.arraycopy(allBytes, 0, this.allBytes, 0, allBytes.length);
    }

    public List<String> execute(String fileName) throws IOException {
        LOG.info("Create distribute file split start: " +fileName);

        // Get the size of the input file.  (Files bigger that
        // Integer.MAX_VALUE will fail here!)
        final int fileSize = this.allBytes.length;

        // Figure out how big each shard will be.  The total size stored
        // will be the file size (8 bytes) plus the file.
        final int storedSize = fileSize;
        final int shardSize = (storedSize + DATA_SHARDS - 1) / DATA_SHARDS;
        final int bufferSize = shardSize * DATA_SHARDS;

        byte[] realFile = new byte[bufferSize];
        System.arraycopy(this.allBytes, 0, realFile, 0, this.allBytes.length);

        // 변수 메모리 초기화
        this.allBytes = null;

        // Make the buffers to hold the shards.
        byte[][] shards = new byte[TOTAL_SHARDS][shardSize];

        // Fill in the data shards
        for (int i = 0; i < DATA_SHARDS; i++) {
            System.arraycopy(realFile, i * shardSize, shards[i], 0, shardSize);
        }

        // Use Reed-Solomon to calculate the parity.
        ReedSolomon reedSolomon = ReedSolomon.create(DATA_SHARDS, PARITY_SHARDS);
        reedSolomon.encodeParity(shards, 0, shardSize);

        // Write out the resulting files.
        List<String> outputFiles = new ArrayList<>();
        for (int i = 0; i < TOTAL_SHARDS; i++) {
            File outputFile = new File(fileName + "." + i);
            FileOutputStream out = new FileOutputStream(outputFile);
            out.write(shards[i]);
            out.close();
            outputFiles.add(outputFile.getAbsolutePath());
            LOG.debug("Create distribute file: " + outputFile);
        }
        LOG.info("Create distribute file completed.");

        return outputFiles;
    }
}
