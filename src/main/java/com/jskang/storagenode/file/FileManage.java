package com.jskang.storagenode.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.response.ResponseResult;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public class FileManage {

    private static Logger LOG = LoggerFactory.getLogger(FileManage.class);
    private static Map<String, List<Path>> fileManage = new HashMap<>();

    /**
     * 파일이 각 서버에 분산된 위치가 저장된 Map 변수 전체 조회
     */
    public static Map<String, List<Path>> getAllFileManage() {
        return fileManage;
    }

    /**
     * 업로드된 파일 목록 조회
     */
    public static Mono<ServerResponse> getFileList() {
        Set<String> fileList = new HashSet<>();
        for (Entry<String, List<Path>> fileName : fileManage.entrySet()) {
            fileList.add(fileName.getKey());
        }
        return ResponseResult.success(fileList);
    }

    /**
     * 특정 파일이 어떤 서버에 분산되어 있는지 서버 목록을 조회
     *
     * @param fileKey 파일 해시 값
     * @return 분산되어 저장된 서버 목록
     */
    public static List<String> getFilePosition(String fileKey) {
        List<String> positions = fileManage.get(fileKey).stream()
            .map(path -> path.toString())
            .collect(Collectors.toList());

        return positions;
    }

    /**
     * 특정 파일의 데이터를 조회
     *
     * @param fileKey 파일 해시 값
     * @return
     */
    public static Stream getFilePostionStream(String fileKey) {
        return fileManage.get(fileKey).stream();
    }

    /**
     * 파일 배포 서버 위치 추가
     *
     * @param fileKey  파일 해시 값
     * @param position 분산 서버 경로
     */
    public static void addFile(String fileKey, List<Path> position) {
        fileManage.put(fileKey, position);
    }

    /**
     * 파일 배포 서버 위치 추가
     *
     * @param fileKey  파일 해시 값
     * @param position 분산 서버 경로
     */
    public static void addFile(String fileKey, Path... position) {
        fileManage.put(fileKey, Arrays.asList(position));
    }

    /**
     * 파일 배포 서버에 분산된 툭정 서버 추가
     *
     * @param fileKey  파일 해시 값
     * @param position 분산 서버 경로
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
     * 업로드된 파일이 존재하는지 여부
     *
     * @param fileKey 파일 해시 값
     * @return 파일이 존재하는 경우 true, 존재하지 않는 경우 false
     */
    public static boolean isFile(String fileKey) {
        return fileManage.get(fileKey) == null ? false : true;
    }

    public static boolean loadFileManager() {
        LOG.info("FileManager.fm read.");

        File file = Paths.get("data", "FileManage.fm").toFile();
        Map<String, Object> data = (Map) Converter.fileToObj(file, new TypeReference<Map>() {
        });
        List<Map<String, Object>> nodeList = (List<Map<String, Object>>) Converter.objToObj(
            data.get("nodeStatusDaos"), new TypeReference<List<Map<String, Object>>>() {
            }
        );

        // 자기 자신의 호스트네임 얻어오기
        SystemInfo systemInfo = new SystemInfo();
        String hostName = systemInfo.getHostName();

        Optional<Map<String, Object>> optional = nodeList.stream()
            .filter(node -> node.get("hostName").equals(hostName))
            .findFirst();

        if (optional.isPresent()) {
            fileManage.putAll((Map<String, List<Path>>) optional.get().get("fileManage"));
            return true;
        } else {
            return false;
        }
    }
}
