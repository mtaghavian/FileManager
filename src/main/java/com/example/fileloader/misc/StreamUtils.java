package com.example.fileloader.misc;

import java.io.*;
import java.security.MessageDigest;

public class StreamUtils {

    public static void writeString(String str, File file) throws IOException {
        writeBytes(str.getBytes("UTF-8"), file);
    }

    public static void writeBytes(byte b[], File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        os.write(b);
        os.flush();
        os.close();
    }

    public static String readString(File file) throws IOException {
        return new String(readBytes(file), "UTF-8");
    }

    public static String readString(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(is, os, false, false);
        return new String(os.toByteArray(), "UTF-8");
    }

    public static byte[] readBytes(File file) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(new FileInputStream(file), os, true, true);
        byte b[] = os.toByteArray();
        return b;
    }

    public static void copy(InputStream is, OutputStream os, boolean closeInput, boolean closeOutput) throws IOException {
        byte b[] = new byte[10000];
        while (true) {
            int r = is.read(b);
            if (r < 0) {
                break;
            }
            os.write(b, 0, r);
        }
        if (closeInput) {
            is.close();
        }
        if (closeOutput) {
            os.flush();
            os.close();
        }
    }

    public static String hash(String s) {
        try {
            return hash(s.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String hash(byte b[]) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(b);
            return toHex(hash, "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public static String toHex(byte b[], String delimeter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String h = String.format("%h", b[i] & 0xff);
            h = (h.length() == 1) ? "0" + h : h;
            sb.append((i == 0) ? h : (delimeter + h));
        }
        return sb.toString();
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            for (File inner : file.listFiles()) {
                delete(inner);
            }
            file.delete();
        } else {
            file.delete();
        }
    }

    public static String toString(Exception ex) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            ex.printStackTrace(writer);
            writer.close();
            return new String(os.toByteArray(), "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }
}
