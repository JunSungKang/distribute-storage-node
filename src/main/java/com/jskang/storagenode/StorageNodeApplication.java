package com.jskang.storagenode;

import com.jskang.storagenode.common.SystemInfo;
import com.jskang.storagenode.node.Node;
import com.jskang.storagenode.node.NodeStatusDao;
import com.jskang.storagenode.node.NodeStatusDaos;
import java.io.File;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StorageNodeApplication implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StorageNodeApplication.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * SpringBoot run port
     */
    private static int settingPort = 8080;

    public static void main(String[] args) {
        // 데이터 파일 생성
        File file = Paths.get("data").toFile();
        if (!file.exists()) {
            LOG.info("Create FileManage.fm");
            file.mkdirs();
        }

        SpringApplication.run(StorageNodeApplication.class, args);
        LOG.info("API Server run completed.");
    }

    /**
     * @param event
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        settingPort = applicationContext.getBean(Environment.class)
            .getProperty("server.port", Integer.class, 8080);
        LOG.info("Port setting completed.");

        if (settingPort != 20040) {
            try {
                new Node().networkJoinRequest();
                LOG.info("Storage node network join completed.");
            } catch (Exception e) {
                LOG.error(e.getMessage());
                System.exit(1);
            }
        } else {
            SystemInfo systemInfo = new SystemInfo();
            String hostName = systemInfo.getHostName();
            // init seed node
            if (
                !NodeStatusDaos
                    .nodeSearch(hostName)
                    .isPresent()
            ) {
                NodeStatusDao addNodeStatusDao = new NodeStatusDao(
                    systemInfo.getHostName(),
                    systemInfo.getDiskTotalSize() - systemInfo.getDiskUseSize()
                );
                addNodeStatusDao.updateFileManage();
                NodeStatusDaos.addNodeStatusDao(addNodeStatusDao);
            }
        }

        LOG.info("storage-node run is completed.");
    }

    public static int getSettingPort() {
        return settingPort;
    }
}
