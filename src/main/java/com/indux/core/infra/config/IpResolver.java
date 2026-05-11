package com.indux.core.infra.config;

import jakarta.servlet.http.HttpServletRequest;

public final class IpResolver {
    private static final String[] CANDIDATE_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Forwarded",
            "CF-Connecting-IP",
            "True-Client-IP"
    };

    public static String resolve(HttpServletRequest request) {
        for (String h : CANDIDATE_HEADERS) {
            String v = request.getHeader(h);
            if (v == null || v.isBlank()) continue;
            String ip = extractFirstIp(v);
            if (!ip.equalsIgnoreCase("unknown")) return ip;
        }
        return request.getRemoteAddr();
    }

    private static String extractFirstIp(String headerValue) {
        if (headerValue.contains(",")) return headerValue.split(",")[0].trim();
        if (headerValue.toLowerCase().startsWith("for=")) {
            String v = headerValue.substring(4).trim();
            if (v.startsWith("\"")) v = v.substring(1, v.indexOf('"', 1));
            else if (v.contains(";")) v = v.substring(0, v.indexOf(';'));
            return v.replace("[", "").replace("]", "");
        }
        return headerValue.trim();
    }
}
