package com.human.digital.digitalhuman.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

/**
 * Sa-Token 权限拦截器配置
 *
 * 三层权限：
 * 1. checkLogin() — 所有接口必须登录（白名单除外）
 * 2. 路径级角色检查 — 按 URL 前缀判断需要的角色
 * 3. 业务级租户隔离 — Service 层从 DB 校验（已有）
 *
 * 角色值：0=超管, 1=学校管理员, 2=教师, 3=学生
 *
 * @Author taoHouChao
 * @Date 15:25 2025/6/9
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 第一层：必须登录
            StpUtil.checkLogin();

            // 第二层：路径级角色检查
            String path = SaHolder.getRequest().getRequestPath();
            Integer role = (Integer) StpUtil.getSession().get("role");
            if (role == null) {
                role = -1;
            }

            // /school/** — 超管专属（学校 CRUD）
            if (path.startsWith("/school/") && role != 0) {
                throw new NotPermissionException("需要超级管理员权限", "role");
            }

            // /backend/** — 后台管理（超管 + 学校管理员）
            if (path.startsWith("/backend/") && role != 0 && role != 1) {
                throw new NotPermissionException("需要管理员权限", "role");
            }

            // /api/school/** — 校本课程管理（超管 + 学校管理员）
            // 教师(role=2)需要读本校课程/统计/科目/班级学生 用于学情监控，放行下列只读端点；
            // 写操作(create/update/delete/addStudent/addClass...)仍限管理员。服务层 pageQuery 已对非管理员走"我的课程"分支。
            if (path.startsWith("/api/school/") && role != 0 && role != 1) {
                String method = SaHolder.getRequest().getMethod();
                if (!(role == 2 && isTeacherSchoolRead(path, method))) {
                    throw new NotPermissionException("需要管理员权限", "role");
                }
            }

            // /api/backend/** — 校本后台管理（学生/班级/科目，超管 + 学校管理员）
            if (path.startsWith("/api/backend/") && role != 0 && role != 1) {
                throw new NotPermissionException("需要管理员权限", "role");
            }

            // /api/teacher/** — 教师管理（deprecated，仍限超管 + 学校管理员）
            if (path.startsWith("/api/teacher/") && role != 0 && role != 1) {
                throw new NotPermissionException("需要管理员权限", "role");
            }

            // /user/** — 后台用户管理（超管 + 学校管理员）
            // 例外：/user/detail 是"查自己资料"，任何登录用户（含教师 role=2）都需要，
            // 前端登录后路由守卫会调它来加载 profile；若拦掉教师会卡在登录环节。
            if (path.startsWith("/user/") && !path.startsWith("/user/detail") && role != 0 && role != 1) {
                throw new NotPermissionException("需要管理员权限", "role");
            }

            // /course/** — 课程管理后台（超管 + 学校管理员）
            // 注意：startsWith("/course/") 不会误伤 /course-material/ 与 /course-template/
            if (path.startsWith("/course/") && role != 0 && role != 1) {
                throw new NotPermissionException("需要管理员权限", "role");
            }

            // /course-template/** — 模板课管理（超管专属）
            if (path.startsWith("/course-template/") && role != 0) {
                throw new NotPermissionException("需要超级管理员权限", "role");
            }

            // /course-material/** — 课程资料后台管理（超管 + 学校管理员）
            if (path.startsWith("/course-material/") && role != 0 && role != 1) {
                throw new NotPermissionException("需要管理员权限", "role");
            }

            // /api/study-monitor/** — 学情监控（超管 + 学校管理员 + 教师）
            if (path.startsWith("/api/study-monitor/") && role != 0 && role != 1 && role != 2) {
                throw new NotPermissionException("需要教师或管理员权限", "role");
            }

            // 其他 /api/** 路径（如 /api/course/, /api/course-material/, /api/question/）
            // 任何登录用户可访问（包括学生）
        }))
        .addPathPatterns("/**")
        .excludePathPatterns(
                "/login",
                "/allSchool",
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/swagger-ui.html",
                "/filetrans/callback/result"
        );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc/openapi-ui/");
    }

    /** 教师(role=2)在校本模块可读的 GET 路径（写操作一律不放行）。 */
    private static final Set<String> TEACHER_SCHOOL_GET_READS = Set.of(
            "/api/school/list",
            "/api/school/stats",
            "/api/school/subject-list",
            "/api/school/class/all-with-students"
    );

    /** 教师(role=2)在校本模块可读的 POST 列表接口（按惯例用 POST 做分页查询的"读"）。 */
    private static final Set<String> TEACHER_SCHOOL_POST_READS = Set.of(
            "/api/school/class/list",
            "/api/school/student/list"
    );

    /**
     * 判断是否为教师可读的校本路径。
     * 放行 GET 课程详情 /api/school/{id}（路径段纯数字）；DELETE /api/school/{id} 不放行。
     */
    private static boolean isTeacherSchoolRead(String path, String method) {
        if (method == null) {
            return false;
        }
        String m = method.toUpperCase();
        if ("GET".equals(m)) {
            if (TEACHER_SCHOOL_GET_READS.contains(path)) {
                return true;
            }
            if (path.startsWith("/api/school/")) {
                String tail = path.substring("/api/school/".length());
                if (!tail.isEmpty() && tail.matches("\\d+")) {
                    return true;
                }
            }
            return false;
        }
        if ("POST".equals(m)) {
            return TEACHER_SCHOOL_POST_READS.contains(path);
        }
        return false;
    }
}
