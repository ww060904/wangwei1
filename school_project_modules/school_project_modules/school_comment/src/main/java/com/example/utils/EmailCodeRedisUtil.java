package com.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码Redis工具类
 * 用于存储和验证邮件验证码
 */
@Component
public class EmailCodeRedisUtil {

    @Autowired
    @Qualifier("saTokenRedisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 验证码前缀
     */
    private static final String EMAIL_CODE_PREFIX = "email:code:";

    /**
     * 验证码尝试次数前缀
     */
    private static final String EMAIL_ATTEMPT_PREFIX = "email:attempt:";

    /**
     * 默认验证码有效期（5分钟）
     */
    private static final long DEFAULT_CODE_EXPIRE = 5;

    /**
     * 默认尝试次数有效期（30分钟）
     */
    private static final long DEFAULT_ATTEMPT_EXPIRE = 30;
    /**
     * 占位符值，表示邮件正在发送中
     */
    private static final String SENDING_PLACEHOLDER = "SENDING";

    /**
     * 创建占位符（邮件发送中）
     * @param email 邮箱地址
     */
    public void createSendingPlaceholder(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        // 设置占位符，有效期设为比实际发送时间稍长（比如2分钟）
        // 如果30秒还没发送完成，占位符会自动过期，允许重新发送
        redisTemplate.opsForValue().set(key, SENDING_PLACEHOLDER, 30, TimeUnit.SECONDS);
    }
    /**
     * 更新占位符为真实验证码
     * @param email 邮箱地址
     * @param code 验证码
     */
    public void updatePlaceholderToCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        // 检查当前是否是占位符
        Object currentValue = redisTemplate.opsForValue().get(key);
        if (SENDING_PLACEHOLDER.equals(currentValue)) {
            // 如果是占位符，更新为真实验证码，并重置过期时间为5分钟
            redisTemplate.opsForValue().set(key, code, DEFAULT_CODE_EXPIRE, TimeUnit.MINUTES);
        } else {
            // 如果不是占位符（可能已经过期或被删除），重新设置
            redisTemplate.opsForValue().set(key, code, DEFAULT_CODE_EXPIRE, TimeUnit.MINUTES);
        }
    }
    /**
     * 检查是否是占位符状态
     * @param email 邮箱地址
     * @return true:正在发送中 false:不是占位符状态或不存在
     */
    public boolean isSendingPlaceholder(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);
        return SENDING_PLACEHOLDER.equals(value);
    }
    /**
     * 存储邮件验证码
     * @param email 邮箱地址
     * @param code 验证码
     */
    public void saveEmailCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, DEFAULT_CODE_EXPIRE, TimeUnit.MINUTES);
    }


    /**
     * 存储邮件验证码（自定义有效期）
     * @param email 邮箱地址
     * @param code 验证码
     * @param expireMinutes 过期时间（分钟）
     */
    public void saveEmailCode(String email, String code, long expireMinutes) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, expireMinutes, TimeUnit.MINUTES);
    }

    /**
     * 获取邮件验证码（排除占位符）
     * @param email 邮箱地址
     * @return 验证码，如果是占位符或不存在返回null
     */
    public String getEmailCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        Object code = redisTemplate.opsForValue().get(key);

        // 如果是占位符，返回null（视为没有有效验证码）
        if (SENDING_PLACEHOLDER.equals(code)) {
            return null;
        }

        return code != null ? code.toString() : null;
    }

    /**
     * 验证邮件验证码
     * @param email 邮箱地址
     * @param inputCode 用户输入的验证码
     * @return true:验证成功 false:验证失败
     */
    public boolean validateEmailCode(String email, String inputCode) {
        String savedCode = getEmailCode(email);

        if (savedCode == null) {
            // 验证码不存在或已过期
            return false;
        }

        // 验证成功后删除验证码（一次性使用）
        if (savedCode.equals(inputCode)) {
            deleteEmailCode(email);
            clearAttemptCount(email);
            return true;
        } else {
            // 记录验证失败次数
            incrementAttemptCount(email);
            return false;
        }
    }

    /**
     * 验证邮件验证码（不删除，用于某些特殊场景）
     * @param email 邮箱地址
     * @param inputCode 用户输入的验证码
     * @return true:验证成功 false:验证失败
     */
    public boolean validateEmailCodeWithoutDelete(String email, String inputCode) {
        String savedCode = getEmailCode(email);
        return savedCode != null && savedCode.equals(inputCode);
    }

    /**
     * 删除邮件验证码
     * @param email 邮箱地址
     */
    public void deleteEmailCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * 检查是否有有效验证码（排除占位符）
     * @param email 邮箱地址
     * @return true:有有效验证码 false:没有或占位符
     */
    public boolean hasValidCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);

        // 如果存在且不是占位符，才算有效验证码
        return value != null && !SENDING_PLACEHOLDER.equals(value);
    }

    /**
     * 获取剩余过期时间（如果是占位符返回负数）
     * @param email 邮箱地址
     * @return 剩余秒数，-1表示占位符，-2表示不存在
     */
    public Long getExpireTime(String email) {
        String key = EMAIL_CODE_PREFIX + email;

        // 先检查是否是占位符
        if (isSendingPlaceholder(email)) {
            return -1L; // 返回-1表示正在发送中
        }

        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 记录验证失败次数（用于防止暴力破解）
     * @param email 邮箱地址
     * @return 当前尝试次数
     */
    public Long incrementAttemptCount(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);

        // 如果是第一次设置，设置过期时间
        if (count != null && count == 1) {
            redisTemplate.expire(key, DEFAULT_ATTEMPT_EXPIRE, TimeUnit.MINUTES);
        }

        return count;
    }

    /**
     * 获取当前尝试次数
     * @param email 邮箱地址
     * @return 尝试次数
     */
    public Integer getAttemptCount(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    /**
     * 清除尝试次数记录
     * @param email 邮箱地址
     */
    public void clearAttemptCount(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * 检查是否尝试次数过多（超过5次）
     * @param email 邮箱地址
     * @param maxAttempts 最大允许尝试次数
     * @return true:超过限制 false:未超过
     */
    public boolean isAttemptLimitExceeded(String email, int maxAttempts) {
        Integer attempts = getAttemptCount(email);
        return attempts >= maxAttempts;
    }

    /**
     * 检查是否尝试次数过多（默认5次）
     * @param email 邮箱地址
     * @return true:超过限制 false:未超过
     */
    public boolean isAttemptLimitExceeded(String email) {
        return isAttemptLimitExceeded(email, 5);
    }
}
