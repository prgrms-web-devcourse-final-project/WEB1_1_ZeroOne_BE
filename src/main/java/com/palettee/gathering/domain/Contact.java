package com.palettee.gathering.domain;

import com.palettee.global.exception.InvalidCategoryException;
import lombok.RequiredArgsConstructor;
import org.webjars.NotFoundException;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Contact {

    ONLINE("온라인"),
    OFFLINE("오프라인"),
    ONOFFLINE("온라인 & 오프라인");


    private final String contact;

    public String getContact(){
        return contact;
    }


    public static Contact findContact(final String input) {
        return Arrays.stream(Contact.values())
                .filter(it -> it.contact.equals(input))
                .findFirst()
                .orElseThrow(()-> InvalidCategoryException.EXCEPTION);
    }


}
