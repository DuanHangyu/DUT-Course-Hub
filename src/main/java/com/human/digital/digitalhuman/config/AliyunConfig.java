package com.human.digital.digitalhuman.config;

import com.alibaba.nls.client.protocol.NlsClient;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.human.digital.digitalhuman.common.utils.TokenUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author taoHouChao
 * @Date 23:22 2025/5/17
 */
@Configuration
@Slf4j
public class AliyunConfig {

    private static final String ENDPOINTNAME = "cn-shanghai";

    public static final String REGIONID = "cn-shanghai";

    public static final String PRODUCT = "nls-filetrans";

    public static final String DOMAIN = "filetrans.cn-shanghai.aliyuncs.com";

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Resource
    private TokenUtils tokenUtils;

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @Bean
    public NlsClient nlsClient(){
        return new NlsClient(tokenUtils.accessToken());
    }

    @Bean("acsClient")
    public IAcsClient acsClient(){
        // 设置endpoint
        try {
            DefaultProfile.addEndpoint(ENDPOINTNAME, REGIONID, PRODUCT, DOMAIN);
        } catch (ClientException e) {
            log.error("创建DefaultAcsClient实例并初始化异常", e);
        }
        // 创建DefaultAcsClient实例并初始化
        DefaultProfile profile = DefaultProfile.getProfile(REGIONID, accessKeyId, accessKeySecret);
        return new DefaultAcsClient(profile);
    }
}
