package com.jskang.storagenode.file;

import com.jskang.storagenode.common.CommonValue;
import com.jskang.storagenode.common.Converter;
import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.common.exception.DataSizeRangeException;
import com.jskang.storagenode.node.NodeStatusDao;
import com.jskang.storagenode.node.NodeStatusDaos;
import com.jskang.storagenode.reedsolomon.ReedSolomonCommon;
import com.jskang.storagenode.reedsolomon.ReedSolomonEncoding;
import com.jskang.storagenode.response.ResponseResult;
import com.jskang.storagenode.smartcontract.SmartContract;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.web3j.abi.datatypes.generated.Bytes32;
import reactor.core.publisher.Mono;

public class Upload {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	private SystemInfo systemInfo = new SystemInfo();

	private ReedSolomonEncoding reedSolomonCommon = null;

	/**
	 * 파일 업로드 요청시, 파일 분산 후 업로드 기능
	 *
	 * @param request 업로드 파일 메타 데이터
	 * @return 업로드 성공시 200 (SUCCESS) 반환, 실패시 500(INTERNAL_SERVER_ERROR) 반환.
	 */
	public Mono<ServerResponse> fileUpload(ServerRequest request) {
		AtomicReference<Mono<ServerResponse>> responseResult = new AtomicReference<>();

		return request.body(BodyExtractors.toMultipartData())
			.map(parts -> {
				Map<String, Part> map = parts.toSingleValueMap();
				if (map.get("file") instanceof FilePart) {
					FilePart filePart = (FilePart) map.get("file");
					String fileName = filePart.filename();

					// Directory create.
					Path dir = Paths.get(CommonValue.UPLOAD_PATH, fileName);
					dir.toFile().mkdirs();

					// Distribute file copy.
					dir = dir.resolve(fileName);
					return filePart.transferTo(dir).then(Mono.just(dir));
				} else {
					return ResponseResult.fail(HttpStatus.BAD_REQUEST);
				}
			})
			.doOnSuccess(mono -> {
				mono.cast(Path.class)
					.subscribe(filePath -> {
						String fileName = filePath.getFileName().toString();

						List<Bytes32> fileNamesBytes32 = new LinkedList<>();
						List<Bytes32> fileHashsBytes32 = new LinkedList<>();

						List<String> outputFiles = this.reedSolomonEncoding(filePath.toString());
						for (String outputFile : outputFiles) {
							try {
								// 파일명
								String realFileName = outputFile.substring(outputFile.lastIndexOf(File.separator)).replaceAll("\\\\", "");
								fileNamesBytes32.add(Converter.stringToBytes32(realFileName));

								// 파일 해시
								InputStream is = Files.newInputStream(Paths.get(outputFile));
								byte[] fileHash = Converter.converterSHA256(is);
								fileHashsBytes32.add(new Bytes32(fileHash));

								// File Upload after, FileManage refresh ready.
								FileManage.addPosition(fileName, realFileName);

								String hostName = this.systemInfo.getHostName();
								NodeStatusDao nodeStatusDao = new NodeStatusDao(
									CommonValue.UPLOAD_PATH,
									hostName,
									this.systemInfo.getDiskTotalSize() - this.systemInfo.getDiskUseSize()
								);
								nodeStatusDao.updateFileManage();

								NodeStatusDaos nodeStatusDaos = FileManage.readFileManager();
								nodeStatusDaos.editNodeStatusDaos(hostName, nodeStatusDao);
								nodeStatusDaos.updateVersion();

								// FileManage refresh.
								try {
									File file = Paths.get("data", "FileManage.fm").toFile();
									FileOutputStream out = new FileOutputStream(file);

									String json = Converter.objToJson(NodeStatusDaos.getNodeStatusAlls());
									out.write(json.getBytes(StandardCharsets.UTF_8));
									out.close();
								} catch (FileNotFoundException e) {
									LOG.error(e.getMessage());
									responseResult.set(
										ResponseResult.fail(HttpStatus.BAD_REQUEST, e.getMessage())
									);
								} catch (IOException e) {
									LOG.error(e.getMessage());
									responseResult.set(
										ResponseResult.fail(HttpStatus.BAD_REQUEST, e.getMessage())
									);
								}
								LOG.debug("file upload success.");
							} catch (NoSuchAlgorithmException e) {
								LOG.error(e.getMessage());
								responseResult.set(
									ResponseResult.fail(HttpStatus.BAD_REQUEST, CommonValue.HASH_ALGORITHM_SHA256 + " hash change fail.")
								);
							} catch (DataSizeRangeException e) {
								LOG.error(e.getMessage());
								responseResult.set(
									ResponseResult.fail(HttpStatus.BAD_REQUEST, "need filename length size == 32.")
								);
							} catch (IOException e) {
								LOG.error(e.getMessage());
								responseResult.set(
									ResponseResult.fail(HttpStatus.BAD_REQUEST, "file get newInputStream fail.")
								);
							}
						}

						// All file upload after, smart-contract run.
						// File smartcontract generate.
						SmartContract smartContract = new SmartContract();
						boolean isCheck = smartContract.connection();
						if (!isCheck) {
							LOG.error("Smart-Contract connection fail.");
						} else {
							byte[] hashValue = null;
							try {
								MessageDigest hash = MessageDigest.getInstance(CommonValue.HASH_ALGORITHM_SHA256);
								hash.update(fileName.getBytes(StandardCharsets.UTF_8));
								hashValue = hash.digest();
							} catch (NoSuchAlgorithmException e) {
								LOG.error(CommonValue.HASH_ALGORITHM_SHA256 + " hash change fail.");
								LOG.debug(e.getMessage());
								responseResult.set(
									ResponseResult.fail(HttpStatus.BAD_REQUEST, CommonValue.HASH_ALGORITHM_SHA256 + " hash change fail.")
								);
							}

							smartContract.setFileHashValue(
								CommonValue.ADMIN_ADDRESS, CommonValue.ADMIN_PASSWORD,
								new Bytes32(hashValue),
								fileNamesBytes32,
								fileHashsBytes32
							);
							LOG.info("smart-contract generate success.");
						}
					});

				// 최종 파일 업로드 완료
				LOG.info("File upload all process complete.");
			})
			.then(ResponseResult.success(""));
	}

	/**
	 * Reed-Solomon encoding processing.
	 *
	 * @param filePath Absolute file path.
	 * @return filepath.
	 */
	private List<String> reedSolomonEncoding(String filePath) {
		LOG.info("(" + filePath + ") ReedSolomon encoding start.");

		// 리드솔로몬 처리할 파일 읽기
		File file = Paths.get(filePath).toFile();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// 읽은 파일 byte로 변환
		LOG.debug("Read file to byte.");
		int fileSize = (int) file.length();
		final int storedSize = fileSize + ReedSolomonCommon.BYTES_IN_INT;
		final int shardSize =
			(storedSize + ReedSolomonCommon.DATA_SHARDS - 1) / ReedSolomonCommon.DATA_SHARDS;
		final int bufferSize = shardSize * ReedSolomonCommon.DATA_SHARDS;

		byte[] allBytes = new byte[bufferSize];
		try {
			fileInputStream.read(allBytes, 0, fileSize);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ReedSolomon Encoding 수행
		LOG.debug("Real reed-solomon encoding.");
		reedSolomonCommon = new ReedSolomonEncoding(allBytes);
		try {
			LOG.info("(" + filePath + ") ReedSolomon encoding end.");
			return reedSolomonCommon.execute(filePath);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}
}
