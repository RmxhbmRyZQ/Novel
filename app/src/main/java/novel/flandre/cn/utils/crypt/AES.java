package novel.flandre.cn.utils.crypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {
    private final static String KEY = "RmxhbmRyZQ495495";

    private static SecretKey generateKey(byte[] key) throws Exception {
        // 根据指定的 RNG 算法, 创建安全随机数生成器
        // SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        // 设置 密钥key的字节数组 作为安全随机数生成器的种子
        // random.setSeed(key);

        // 创建 AES算法生成器
        // KeyGenerator gen = KeyGenerator.getInstance("AES");
        // 初始化算法生成器
        // gen.init(128, random);

        // 生成 AES密钥对象, 也可以直接创建密钥对象: return new SecretKeySpec(key, ALGORITHM);
        // return gen.generateKey();
        return new SecretKeySpec(key, "AES");
    }

    public static byte[] encrypt(byte[] data) throws Exception{
        // 生成密钥对象
        SecretKey secKey = generateKey(KEY.getBytes());

        // 获取 AES 密码器
        Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
        // 初始化密码器（加密模型）
        cipher.init(Cipher.ENCRYPT_MODE, secKey);

        // 加密数据, 返回密文
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data) throws Exception {
        // 生成密钥对象
        SecretKey secKey = generateKey(KEY.getBytes());

        // 获取 AES 密码器
        Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");
        // 初始化密码器（解密模型）
        cipher.init(Cipher.DECRYPT_MODE, secKey);

        // 解密数据, 返回明文
        return cipher.doFinal(data);
    }
}
