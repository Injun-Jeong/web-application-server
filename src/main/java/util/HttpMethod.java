package util;

/**
 * 상수 값이 서로 연관되어 있는 경우 자바의 enum을 쓰기 적합
 */
public enum HttpMethod {
    GET, POST;

    public boolean isGet() {
        return this == GET;
    }

    public boolean isPost() {
        return this == POST;
    }
}
