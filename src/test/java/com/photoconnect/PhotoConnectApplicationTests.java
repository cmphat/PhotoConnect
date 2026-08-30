package com.photoconnect;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
class PhotoConnectApplicationTests {

    @Test
    void contextLoads() {
    }

}
