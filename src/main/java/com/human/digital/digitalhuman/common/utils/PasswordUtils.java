package com.human.digital.digitalhuman.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.regex.Pattern;

/**
 * 密码哈希工具：BCrypt 单向哈希，每次 salt 不同。
 *
 * <p>支持的兼容性：
 * <ul>
 *   <li>写入：始终用 {@link #encode(String)} 存储 BCrypt 哈希</li>
 *   <li>校验：BCrypt 哈希用 BCrypt 比对；遗留明文用 equals 比对（用于一次性迁移）</li>
 * </ul>
 *
 * @Author taoHouChao
 * @Date 10:00 2026/6/19
 */
@Slf4j
public final class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder(10);

    /**
     * BCrypt 哈希完整格式校验：$2a$/$2b$/$2y$ + cost + 22 字符 salt + 31 字符 hash。
     * 用于准确识别已迁移密码，避免误判恰好以 $2 开头的明文。
     */
    private static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private PasswordUtils() {
    }

    /**
     * 对明文密码做 BCrypt 哈希。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 哈希字符串
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码。
     * <p>对已迁移（BCrypt）的哈希使用 BCrypt.matches；对遗留明文使用 equals 比对，
     * 调用方可据此触发迁移。
     *
     * @param rawPassword     用户输入的明文密码
     * @param storedPassword  数据库中存储的值（可能是 BCrypt 哈希或遗留明文）
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return ENCODER.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    /**
     * 判断存储值是否已经是 BCrypt 哈希。
     *
     * @param storedPassword 数据库存储值
     * @return true 表示已是 BCrypt 哈希
     */
    public static boolean isBcryptHash(String storedPassword) {
        return storedPassword != null && BCRYPT_PATTERN.matcher(storedPassword).matches();
    }

    /**
     * 如有必要则将明文密码转为 BCrypt 哈希。
     *
     * @param rawOrHashed 可能是明文或已哈希的密码
     * @return 总是返回 BCrypt 哈希
     */
    public static String ensureEncoded(String rawOrHashed) {
        if (rawOrHashed == null) {
            return null;
        }
        if (isBcryptHash(rawOrHashed)) {
            return rawOrHashed;
        }
        return encode(rawOrHashed);
    }
}
