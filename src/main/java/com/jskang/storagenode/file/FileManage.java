package com.jskang.storagenode.file;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileManage {

    private static Map<String, List<Path>> fileManage = new HashMap<>();

    /**
     * all File distribute server get position.
     */
    public static Map<String, List<Path>> getAllFileManage() {
        return fileManage;
    }

    /**
     * file distribute server get position.
     *
     * @param fileKey file key (hash)
     * @return
     */
    public static List<String> getFilePosition(String fileKey) {
        List<String> positions = fileManage.get(fileKey).stream()
            .map(path -> path.toString())
            .collect(Collectors.toList());

        return positions;
    }

    /**
     * file distribute server get position stream.
     *
     * @param fileKey file key (hash)
     * @return
     */
    public static Stream getFilePostionStream(String fileKey) {
        return fileManage.get(fileKey).stream();
    }

    /**
     * file distribute server add position (whole array).
     *
     * @param fileKey  file key (hash)
     * @param position distribute path.
     */
    public static void addFile(String fileKey, List<Path> position) {
        fileManage.put(fileKey, position);
    }

    /**
     * file distribute server add position (whole array).
     *
     * @param fileKey  file key (hash)
     * @param position distribute path.
     */
    public static void addFile(String fileKey, Path... position) {
        fileManage.put(fileKey, Arrays.asList(position));
    }

    /**
     * file distribute server add single position.
     *
     * @param fileKey  file key (hash)
     * @param position distribute path.
     */
    public static void addPosition(String fileKey, Path position) {
        List<Path> positions = fileManage.get(fileKey);
        if (positions == null) {
            positions = new ArrayList<>();
        }
        positions.add(position);

        fileManage.put(fileKey, positions);
    }

    /**
     * Check if uploaded file exists.
     *
     * @param fileKey file key (hash)
     * @return
     */
    public static boolean isFile(String fileKey) {
        return fileManage.get(fileKey) == null ? false : true;
    }
}
