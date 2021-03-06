package com.nowcoder.async;

/**
 * 事件的类型（点赞、评论、登陆、邮件发送）
 * @author xiezhiping
 *
 */
public enum EventType {
    LIKE(0),
    COMMENT(1),
    LOGIN(2),
    MAIL(3);

    private int value;

    EventType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
