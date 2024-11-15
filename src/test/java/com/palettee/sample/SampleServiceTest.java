package com.palettee.sample;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;

@SpringBootTest
class SampleServiceTest {

    @Autowired
    private SampleService sampleService;

    @Test
    void sampleTest1()	{

        assertThat(sampleService.getOne())
                .isNotEqualTo(1);
    }

    @Test
    void sampleTest2()	{

        assertThat(sampleService.getFalse())
                .isFalse();
    }

    @Test
    void sampleTest3()	{


    }

    @Test
    void sampleTest4()	{
    }

    @Test
    void sampleTest5()	{
    }

    @Test
    void failOnPurpose()	{
        assertThat(sampleService.getFalse())
                .isTrue();
    }
}