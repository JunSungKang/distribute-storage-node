package com.jskang.storagenode;

import com.jskang.storagenode.node.Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StorageNodeApplication {

    public static void main(String[] args) throws Exception {
        Module.networkSeedConnect();
        System.out.println("Initial connection Seed Node setup has been completed.");

        SpringApplication.run(StorageNodeApplication.class, args);
        System.out.println("storage-node operation is completed.");

        // TODO: 초기 노드의 경우 요청하지 않도록 하거나, 요청해도 응답없는 경우 무시하는 것으로 변경해야함
        new Module().networkJoinRequest();
        System.out.println("storage-node network join success.");
    }

}
