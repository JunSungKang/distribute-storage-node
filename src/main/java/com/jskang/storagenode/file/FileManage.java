package com.jskang.storagenode.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.node.NodeStatusDaos;
import com.jskang.storagenode.response.ResponseResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static Map<String, List<String>> fileManage = new HashMap<>();

    /**
     * 파일이 각 서버에 분산된 위치가 저장된 Map 변수 전체 조회
     */
    public static Map<String, List<String>> getAllFileManage() {
        return fileManage;
    }

    /**
     * 업로드된 파일 목록 조회
     */
    public static Mono<ServerResponse> getFileList() {
        Set<String> fileList = new HashSet<>();
        for (Entry<String, List<String>> fileName : fileManage.entrySet()) {
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
        List<String> positions = new ArrayList<>();
        if (fileManage.get(fileKey) == null) {
            return new ArrayList<>();
        }

        int size = fileManage.get(fileKey).size();
        for (int i=0; i<size; i++) {
            positions.add("upload"+File.separator.concat( String.valueOf(fileManage.get(fileKey).get(i)) ) );
        }

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
     * @param fileName 분산 파일명
     */
    public static void addFile(String fileKey, List<String> fileName) {
        fileManage.put(fileKey, fileName);
    }

    /**
     * 파일 배포 서버 위치 추가
     *
     * @param fileKey  파일 해시 값
     * @param fileName 분산 파일명
     */
    public static void addFile(String fileKey, String... fileName) {
        fileManage.put(fileKey, Arrays.asList(fileName));
    }

    /**
     * 파일 배포 서버에 분산된 툭정 서버 추가
     *
     * @param fileKey  파일 해시 값
     * @param fileName 분산 파일명
     */
    public static void addPosition(String fileKey, String fileName) {
        List<String> fileNames = fileManage.get(fileKey);
        if (fileNames == null) {
            fileNames = new ArrayList<>();
        }
        if (fileNames.contains(fileName)) {
            // 동일한 파일명이 이미 업로드된 경우 추가되지 않도록 처리.
            return;
        }
        fileNames.add(fileName);

        fileManage.put(fileKey, fileNames);
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

    public static int loadFileManager() {
        LOG.info("FileManager.fm read.");

        Map<String, Object> data = new HashMap<>();
        File file = Paths.get("data", "FileManage.fm").toFile();
        try {
            if (!file.isFile()) {
                // 최초 실행의 경우 파일이 없으므로 파일 생성
                file.createNewFile();
                data = NodeStatusDaos.getNodeStatusAlls();
            }
        } catch (IOException e) {
            LOG.error("file create error.");
            LOG.debug(e.getMessage());
        } catch (Exception e) {
            LOG.error("file create error.");
            LOG.debug(e.getMessage());
        }

        List<Map<String, Object>> nodeList = new ArrayList<>();
        if (file.length() > 3) {
            data = (Map) Converter.fileToObj(file, new TypeReference<Map>() {});
            nodeList = (List<Map<String, Object>>) Converter.objToObj(
                data.get("nodeStatusDaos"), new TypeReference<List<Map<String, Object>>>() {}
            );
        }
        if (nodeList == null) {
            nodeList = new ArrayList<>();
        }

        // 자기 자신의 호스트네임 얻어오기
        SystemInfo systemInfo = new SystemInfo();
        String hostName = systemInfo.getHostName();

        Optional<Map<String, Object>> optional = nodeList.stream()
            .filter(node -> node.get("hostName").equals(hostName))
            .findFirst();

        if (optional.isPresent()) {
            fileManage.putAll((Map<String, List<String>>) optional.get().get("fileManage"));
            return 0;
        } else {
            fileManage.putAll(new HashMap<>());
            return -1;
        }
    }

    /**
     * 기존의 파일 매니저의 가장 최신 상태를 읽어옴
     * @return
     */
    public static NodeStatusDaos readFileManager() {
        LOG.info("FileManager.fm read.");

        File file = Paths.get("data", "FileManage.fm").toFile();
        return (NodeStatusDaos)Converter.fileToObj(file, new TypeReference<NodeStatusDaos>() {});
    }
}
