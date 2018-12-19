package com.artezio.arttime.utils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {
    public static List<String> splitByComma(@NotNull String string) {
        return Stream
                .of(string.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
