package com.jskang.storagenode;

import com.jskang.storagenode.node.Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StorageNodeApplication {

    public static void main(String[] args) {
        Module.networkSeedConnect();
        System.out.println("Initial connection Seed Node setup has been completed.");

        SpringApplication.run(StorageNodeApplication.class, args);
        System.out.println("storage-node operation is completed.");
    }

}
