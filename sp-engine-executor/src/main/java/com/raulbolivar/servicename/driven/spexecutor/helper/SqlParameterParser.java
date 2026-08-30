package com.raulbolivar.servicename.driven.spexecutor.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlParameterParser {

    private static final Pattern PARAMETER_PATTERN =
            Pattern.compile("(?<!:):([a-zA-Z][a-zA-Z0-9_]*)");

    private SqlParameterParser() {
    }

    public static ParsedSql parse(String sql,
                                  Map<String, Object> parameters) {
        Matcher matcher = PARAMETER_PATTERN.matcher(sql);
        StringBuilder parsedSql = new StringBuilder();
        List<SqlParameter> orderedParameters = new ArrayList<>();

        while (matcher.find()) {
            String parameterName = matcher.group(1);

            if (!parameters.containsKey(parameterName)) {
                throw new IllegalArgumentException("Missing SQL parameter: " + parameterName);
            }

            matcher.appendReplacement(parsedSql, Matcher.quoteReplacement("@" + parameterName));
            orderedParameters.add(new SqlParameter(parameterName, parameters.get(parameterName)));
        }

        matcher.appendTail(parsedSql);

        return new ParsedSql(parsedSql.toString(), orderedParameters);
    }

    public record ParsedSql(String sql, List<SqlParameter> parameters) {}

    public record SqlParameter(String name, Object value) {}
}