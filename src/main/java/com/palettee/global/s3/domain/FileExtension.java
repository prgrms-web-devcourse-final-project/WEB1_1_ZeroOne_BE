package com.palettee.global.s3.domain;


public enum FileExtension {
    JPG("jpg"),
    HEIC("heic"),
    JPEG("jpeg"),
    PNG("png");

    private final String extension;

    FileExtension(String extension) {
        this.extension = extension;
    }

    public static boolean isValidExtension(String fileExt) {
        for (FileExtension ext : FileExtension.values()) {
            if (fileExt.toLowerCase().equals(ext.extension)) {
                return true;
            }
        }
        return false;
    }
}
