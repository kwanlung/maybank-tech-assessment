package com.maybank.maybank_assessment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class MaybankAssessmentApplicationTests {

    @Test
    void mainMethodRunsWithoutExceptions() {
        assertDoesNotThrow(() -> MaybankAssessmentApplication.main(new String[]{}));
    }

}
