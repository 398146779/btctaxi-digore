package com.btctaxi.gate.util;

import javax.servlet.http.HttpServletRequest;

public class IP {

    public static long ip2long(String ip) {
        if (ip.contains(", ")) {
            try {
                ip = ip.split(", ")[0];
            } catch (Exception e) {
            }
        }

        long result = 0;
        try {
            if (ip.contains(".")) {
                String paras[] = ip.split("\\.");
                result = Integer.parseInt(paras[0]) * 255 * 255 * 255 + Integer.parseInt(paras[1]) * 255 * 255 + Integer.parseInt(paras[2]) * 255 + Integer.parseInt(paras[3]);
            }
        } catch (Exception e) {
        }
        return result;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
