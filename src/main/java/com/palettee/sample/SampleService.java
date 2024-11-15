package com.palettee.sample;

import org.springframework.stereotype.*;

@Service
public class SampleService {

    public int getOne() {
        return 1;
    }

    public boolean getFalse() {
        return false;
    }

    public String helloWorld() {
        return "Hello World";
    }
}
