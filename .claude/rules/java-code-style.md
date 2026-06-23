# 代码风格规范

本文档定义了本项目的代码编写规范，所有开发者应遵循此规范以保持代码一致性。

---

## 1. 类注释规范

### 1.1 类注释格式

所有类必须添加 Javadoc 风格的注释，使用 `@Author` 和 `@Date` 格式：

```java
/**
 * 类名简述
 *
 * @Author [作者名]
 * @Date [日期] [时间]
 */
```

**示例**：

```java
/**
 * 业务异常类
 * 用于封装业务逻辑相关的异常信息
 *
 * @Author taoHouChao
 * @Date 12:26 2025/6/7
 */
@Getter
public class BusinessException extends RuntimeException {
    // ...
}
```

### 1.2 作者名规范

- 使用拼音或英文名
- 项目内统一使用：`taoHouChao`

---

## 2. 方法注释规范

### 2.1 Service 层公开方法

Service 层的公开方法必须添加 Javadoc 注释，说明方法功能、参数和返回值：

```java
/**
 * 方法功能描述
 *
 * @param 参数名 参数说明
 * @return 返回值说明
 */
public ReturnType methodName(Type param) {
    // ...
}
```

**示例**：

```java
/**
 * 根据课程节点ID和学生ID生成考核问题
 *
 * @param courseNodeId 课程节点ID
 * @param studentId    学生ID
 * @return 包含问题详情的对象
 */
public QuestionDTO generateQuestion(Integer courseNodeId, Integer studentId) {
    // ...
}
```

### 2.2 Controller 层方法

Controller 层方法使用 `@Operation` 注解描述接口功能，不需要额外的 Javadoc 注释：

```java
@GetMapping("/assess-detail")
@Operation(summary = "考核详情接口")
public AssessDetailDTO nodeAssessDetail(@RequestParam("courseNodeId") @Schema(description = "课程节点ID") Integer courseNodeId) {
    // ...
}
```

### 2.3 代码内部注释

使用行内注释解释代码逻辑，注释内容应简洁明了：

```java
// 获取课程节点信息
CourseNodePO courseNode = courseNodeService.getById(courseNodeId);

// 检查课程节点是否存在
if (courseNode == null) {
    throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
}
```

### 2.4 TODO 注释

未完成的代码使用 TODO 注释，格式如下：

```java
// TODO: [作者] 描述内容
```

**示例**：

```java
// TODO: taoHouChao 待实现缓存逻辑
```

---

## 3. API 接口注解规范

### 3.1 Controller 类注解

```java
@RestController
@RequestMapping("/api/xxx")  // 前台接口使用 /api/ 前缀
@RequiredArgsConstructor    // Lombok 构造函数注入
@Slf4j                      // Lombok 日志对象
@Tag(name = "模块名称", description = "模块描述")
@Validated                  // 启用参数校验
public class XxxController {
    // ...
}
```

### 3.2 请求方法注解

| 请求类型 | 注解 | 示例 |
|----------|------|------|
| GET 查询 | `@GetMapping` | `@GetMapping("/detail")` |
| POST 创建 | `@PostMapping` | `@PostMapping("/create")` |
| PUT 更新 | `@PutMapping` | `@PutMapping("/update")` |
| DELETE 删除 | `@DeleteMapping` | `@DeleteMapping("/{id}")` |

### 3.3 参数注解

**GET 请求 - URL 参数**：

```java
@GetMapping("/detail")
@Operation(summary = "详情接口")
public XxxDTO detail(@RequestParam("id") @Schema(description = "ID") Integer id) {
    // ...
}
```

**POST/PUT 请求 - JSON Body**：

```java
@PostMapping("/create")
@Operation(summary = "创建接口")
public Boolean create(@RequestBody XxxCmd cmd) {
    // ...
}
```

**路径参数**：

```java
@GetMapping("/{id}")
@Operation(summary = "根据ID查询")
public XxxDTO getById(@PathVariable("id") @Schema(description = "ID") Integer id) {
    // ...
}
```

### 3.4 Swagger 注解

- `@Tag`：用于 Controller 类，描述功能模块
- `@Operation`：用于方法，描述接口功能
- `@Schema`：用于参数，描述字段含义

---

## 4. 异常使用规范

### 4.1 自定义异常

自定义异常必须继承 `RuntimeException`：

```java
/**
 * 业务异常类
 *
 * @Author taoHouChao
 * @Date 12:26 2025/6/7
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String message;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ErrorCodeEnums errorCodeEnums) {
        super(errorCodeEnums.getMessage());
        this.code = errorCodeEnums.getCode();
        this.message = errorCodeEnums.getMessage();
    }
}
```

### 4.2 错误码定义

使用枚举类定义错误码，按照以下分类：

```java
@Getter
@AllArgsConstructor
public enum ErrorCodeEnums {
    SUCCESS(0, "成功"),
    GET_RESPONSE_ERROR(99, "获取响应失败"),

    // 1xx - 学生相关
    STUDENT_EXIST(101, "学生已存在"),
    STUDENT_NOT_EXIST(104, "学生不存在"),

    // 3xx - 课程相关
    COURSE_NOT_EXIST(300, "课程不存在"),
    COURSE_NODE_NOT_EXIST(301, "课程节点不存在"),

    // 7xx - AI 相关
    AI_CALL_ERROR(701, "AI调用错误"),
    ;

    private final int code;
    private final String message;
}
```

**错误码分类规则**：

| 区间 | 类别 |
|------|------|
| 0xx | 通用/成功 |
| 1xx | 学生相关 |
| 2xx | 用户相关 |
| 3xx | 课程相关 |
| 4xx | 学习记录相关 |
| 5xx | 文件相关 |
| 6xx | 考核相关 |
| 7xx-9xx | AI/其他 |

### 4.3 全局异常处理

使用 `@RestControllerAdvice` 进行全局异常处理：

```java
@RestControllerAdvice
@Slf4j
public class GlobalException {

    @ExceptionHandler(BusinessException.class)
    public BaseResult<Void> handleException(BusinessException e) {
        log.error("e", e);
        return BaseResult.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResult<Void> handleException(Exception e) {
        log.error("e", e);
        return BaseResult.error(500, "系统异常");
    }
}
```

### 4.4 抛出异常的方式

```java
// 推荐：使用错误码枚举
throw new BusinessException(ErrorCodeEnums.COURSE_NODE_NOT_EXIST);
```

---

## 5. 参数校验规范

### 5.1 DTO/Cmd 类校验注解

```java
@Data
public class XxxCmd {
    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    @Schema(description = "ID")
    @NotNull(message = "ID不能为空")
    private Integer id;

    @Schema(description = "描述")
    @Size(max = 500, message = "描述不能超过500字")
    private String description;
}
```

**常用校验注解**：

| 注解 | 适用类型 | 说明 |
|------|----------|------|
| `@NotNull` | 任意 | 不能为 null |
| `@NotEmpty` | String/集合 | 不能为空 |
| `@NotBlank` | String | 不能为空（去除空格后） |
| `@Size` | String/集合 | 长度/大小范围 |
| `@Pattern` | String | 正则匹配 |
| `@Min`/`@Max` | 数字 | 数值范围 |
| `@Email` | String | 邮箱格式 |

### 5.2 Controller 启用校验

在 Controller 类上添加 `@Validated` 注解：

```java
@Validated
public class XxxController {
    // ...
}
```

---

## 6. 命名规范

### 6.1 类命名

| 类型 | 后缀 | 示例 |
|------|------|------|
| Controller | `XxxController` | `AssessController` |
| Service | `XxxService` / `XxxAppService` | `AssessService` |
| Manager | `XxxManager` | `CacheManager` |
| Command | `XxxCmd` | `StartNodeAssessCmd` |
| DTO | `XxxDTO` | `AssessDetailDTO` |
| PO | `XxxPO` | `CourseNodePO` |
| Exception | `XxxException` | `BusinessException` |
| Enums | `XxxEnums` | `ErrorCodeEnums` |

### 6.2 方法命名

| 操作 | 动词 | 示例 |
|------|------|------|
| 查询 | `get`, `query`, `find` | `getById`, `queryList` |
| 创建 | `create`, `save` | `createUser`, `saveCourse` |
| 更新 | `update`, `modify` | `updateStatus` |
| 删除 | `delete`, `remove` | `deleteById` |

---

## 7. 日志规范

### 7.1 日志对象

使用 Lombok 的 `@Slf4j` 注解生成日志对象：

```java
@Slf4j
public class XxxService {
    // ...
}
```

### 7.2 日志级别使用

```java
// 信息记录 - 关键业务流程
log.info("start generate question");
log.info("courseNode:{}", courseNode);

// 警告记录 - 需要关注但不中断业务
log.warn("cache miss, key:{}", key);

// 错误记录 - 异常信息
log.error("generateQuestion error", e);
```

### 7.3 日志格式

```java
// 使用占位符 {} 拼接参数，避免字符串拼接
log.info("action:{}, param:{}", action, param);

// 记录异常时，异常对象作为第二个参数
log.error("error message", exception);
```

---

## 8. 响应规范

### 8.1 响应包装

所有 API 响应使用 `BaseResult<T>` 包装：

```java
@Data
public class BaseResult<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public static <T> BaseResult<T> success(T data) {
        BaseResult<T> result = new BaseResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> BaseResult<T> error(int code, String message) {
        BaseResult<T> result = new BaseResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
```

### 8.2 响应示例

```java
// 成功响应
return BaseResult.success(data);

// 错误响应（使用错误码枚举）
return BaseResult.error(ErrorCodeEnums.COURSE_NOT_EXIST.getCode(),
                        ErrorCodeEnums.COURSE_NOT_EXIST.getMessage());
```

---

## 9. 常量定义规范

### 9.1 常量类

同类常量使用 `private static final` 定义：

```java
public class XxxConstants {
    private XxxConstants() {}

    private static final String QUESTION_TEMPLATE = """
            作为大学考核专家，请根据以下课程内容设计问题：
            %s
            """;

    private static final int DEFAULT_TIMEOUT = 30000;
}
```

### 9.2 多行字符串常量

使用 Java 15+ 的文本块（Text Blocks）：

```java
private static final String PROMPT_TEMPLATE = """
        作为大学考核专家，请根据以下课程视频内容设计一个考核题目：

        **课程内容摘要**:
        %s

        **要求**:
        1. 问题要准确反映课程内容
        2. 难度要适中
        """;
```

---

## 10. 导入规范

### 10.1 import 顺序（建议）

1. Java 标准库 (`java.*`)
2. Spring 框架 (`org.springframework.*`)
3. 第三方库 (`com.*`, `org.*`)
4. 项目内部包 (`com.human.digital.*`)

```java
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
```

---

## 附录：错误码参考

| 区间 | 类别 |
|------|------|
| 0xx | 通用/成功 |
| 1xx | 学生相关 |
| 2xx | 用户相关 |
| 3xx | 课程相关 |
| 4xx | 学习记录相关 |
| 5xx | 文件相关 |
| 6xx | 考核相关 |
| 7xx-9xx | AI/其他 |
