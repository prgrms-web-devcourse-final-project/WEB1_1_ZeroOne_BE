package com.palettee.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaletteException extends RuntimeException {
    private ErrorCode errorCode;
}
