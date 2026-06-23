package com.human.digital.digitalhuman.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.human.digital.digitalhuman.common.enums.ErrorCodeEnums;
import com.human.digital.digitalhuman.common.exception.BusinessException;
import com.human.digital.digitalhuman.common.utils.OssClientUtils;
import com.human.digital.digitalhuman.repository.po.CourseStudentCommentPO;
import com.human.digital.digitalhuman.repository.po.StudentPO;
import com.human.digital.digitalhuman.repository.po.UserPO;
import com.human.digital.digitalhuman.repository.service.CourseStudentCommentService;
import com.human.digital.digitalhuman.repository.service.StudentService;
import com.human.digital.digitalhuman.repository.service.UserService;
import com.human.digital.digitalhuman.service.model.dto.FileDTO;
import com.human.digital.digitalhuman.service.model.request.CommentDeleteCmd;
import com.human.digital.digitalhuman.service.model.request.CommentReplySaveCmd;
import com.human.digital.digitalhuman.service.model.request.CommentSaveCmd;
import com.human.digital.digitalhuman.service.model.response.CommentDTO;
import com.human.digital.digitalhuman.service.model.response.CommentResponse;
import com.human.digital.digitalhuman.service.model.response.ReplyDTO;
import com.human.digital.digitalhuman.service.model.response.ReplyMoreDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @USER taoHouChao
 * @DATE 23:22 2025/9/12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseStudentCommentAppService {

    private final CourseStudentCommentService courseStudentCommentService;

    private final OssClientUtils  ossClientUtils;

    private final UserService userService;

    private final StudentService studentService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean studentSaveComment(CommentSaveCmd saveCmd) {
        int studentId = StpUtil.getLoginIdAsInt();
        CourseStudentCommentPO comment = CourseStudentCommentPO.builder()
                .courseId(saveCmd.getCourseId())
                .courseNodeId(saveCmd.getCourseNodeId() == null ? 0 : saveCmd.getCourseNodeId())
                .userId(studentId)
                .userType(0)
                .content(saveCmd.getContent())
                .files(saveCmd.getFiles() == null ? null : JSONUtil.toJsonStr(saveCmd.getFiles()))
                .pictures(saveCmd.getPictures() == null ? null : JSONUtil.toJsonStr(saveCmd.getPictures()))
                .build();
        courseStudentCommentService.save(comment);
        Integer id = comment.getId();
        comment.setRootId(id);
        courseStudentCommentService.updateById(comment);
        return true;
    }

    public Boolean studentSaveReply(CommentReplySaveCmd saveCmd) {
        int studentId = StpUtil.getLoginIdAsInt();
        Integer commentId = saveCmd.getCommentId();
        CourseStudentCommentPO comment = courseStudentCommentService.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_STUDENT_COMMENT_NOT_EXIST);
        }

        CourseStudentCommentPO reply = CourseStudentCommentPO.builder()
                .courseId(saveCmd.getCourseId())
                .courseNodeId(saveCmd.getCourseNodeId() == null ? 0 : saveCmd.getCourseNodeId())
                .userId(studentId)
                .userType(0)
                .rootId(comment.getRootId())
                .parentId(comment.getId())
                .content(saveCmd.getContent())
                .files(saveCmd.getFiles() == null ? null : JSONUtil.toJsonStr(saveCmd.getFiles()))
                .pictures(saveCmd.getPictures() == null ? null : JSONUtil.toJsonStr(saveCmd.getPictures()))
                .build();
        courseStudentCommentService.save(reply);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean teacherSaveComment(CommentSaveCmd saveCmd) {
        int teacherId = StpUtil.getLoginIdAsInt();
        CourseStudentCommentPO comment = CourseStudentCommentPO.builder()
                .courseId(saveCmd.getCourseId())
                .userId(teacherId)
                .userType(1)
                .content(saveCmd.getContent())
                .files(saveCmd.getFiles() == null ? null : JSONUtil.toJsonStr(saveCmd.getFiles()))
                .pictures(saveCmd.getPictures() == null ? null : JSONUtil.toJsonStr(saveCmd.getPictures()))
                .build();
        courseStudentCommentService.save(comment);
        Integer id = comment.getId();
        comment.setRootId(id);
        courseStudentCommentService.updateById(comment);
        return true;
    }

    public Boolean teacherSaveReply(CommentReplySaveCmd saveCmd) {
        int teacherId = StpUtil.getLoginIdAsInt();
        Integer commentId = saveCmd.getCommentId();
        CourseStudentCommentPO comment = courseStudentCommentService.getById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCodeEnums.COURSE_STUDENT_COMMENT_NOT_EXIST);
        }

        CourseStudentCommentPO reply = CourseStudentCommentPO.builder()
                .courseId(saveCmd.getCourseId())
                .userId(teacherId)
                .userType(1)
                .rootId(comment.getRootId())
                .parentId(comment.getId())
                .content(saveCmd.getContent())
                .files(saveCmd.getFiles() == null ? null : JSONUtil.toJsonStr(saveCmd.getFiles()))
                .pictures(saveCmd.getPictures() == null ? null : JSONUtil.toJsonStr(saveCmd.getPictures()))
                .build();
        courseStudentCommentService.save(reply);
        return true;
    }

    public CommentResponse commentList(Integer courseId, Integer courseNodeId, Integer after) {
        int queryCourseNodeId = 0;
        if (courseNodeId != null) {
            queryCourseNodeId = courseNodeId;
        }
        CommentResponse response = new CommentResponse();
        LambdaQueryWrapper<CourseStudentCommentPO> queryWrapper = Wrappers.lambdaQuery(CourseStudentCommentPO.class)
                .eq(CourseStudentCommentPO::getCourseId, courseId)
                .eq(CourseStudentCommentPO::getCourseNodeId, queryCourseNodeId)
                .eq(CourseStudentCommentPO::getParentId, 0);
        long count = courseStudentCommentService.count(queryWrapper);
        response.setTotal(count);
        List<CourseStudentCommentPO> comments = courseStudentCommentService.list(queryWrapper
                .lt(after != null, CourseStudentCommentPO::getId, after)
                .orderByDesc(CourseStudentCommentPO::getId)
                .last("limit 10"));
        response.setComments(comments.parallelStream()
                .map(item -> {
                    Integer userType = item.getUserType();
                    Integer userId = item.getUserId();
                    String name;
                    if (Objects.equals(userType, 0)) {
                        StudentPO student = studentService.getById(userId);
                        name = student == null ? "" : student.getStudentName();
                    } else {
                        UserPO user = userService.getById(userId);
                        name = user == null ? "" : user.getUserName();
                    }
                    List<CourseStudentCommentPO> replies = courseStudentCommentService.list(Wrappers.lambdaQuery(CourseStudentCommentPO.class)
                            .eq(CourseStudentCommentPO::getRootId, item.getId())
                            .gt(CourseStudentCommentPO::getParentId, 0)
                            .orderByAsc(CourseStudentCommentPO::getId)
                            .last("limit 3"));

                    long total = courseStudentCommentService.count(Wrappers.lambdaQuery(CourseStudentCommentPO.class)
                            .gt(CourseStudentCommentPO::getParentId, 0)
                            .eq(CourseStudentCommentPO::getRootId, item.getId()));

                    ReplyMoreDTO replyMore = new ReplyMoreDTO();
                    List<ReplyDTO> replyDTOS = replies.parallelStream()
                            .map(this::getReplyDTO).toList();
                    replyMore.setReplies(replyDTOS);
                    replyMore.setHasMore(total > replies.size());

                    String files = item.getFiles();
                    List<FileDTO> fileDTOS = null;
                    if (StringUtils.isNotBlank(files)) {
                        fileDTOS = JSONUtil.toList(files, FileDTO.class);
                        for (FileDTO fileDTO : fileDTOS) {
                            fileDTO.setUrl(ossClientUtils.getSignedUrl(fileDTO.getUrl()));
                        }
                    }
                    String pictures = item.getPictures();
                    List<FileDTO> pictureDTOS = null;
                    if (StringUtils.isNotBlank(pictures)) {
                        pictureDTOS = JSONUtil.toList(pictures, FileDTO.class);
                        for (FileDTO fileDTO : pictureDTOS) {
                            fileDTO.setUrl(ossClientUtils.getSignedUrl(fileDTO.getUrl()));
                        }
                    }
                    return CommentDTO.builder()
                            .id(item.getId())
                            .userName(name)
                            .userType(userType)
                            .content(item.getContent())
                            .createTime(item.getCreateTime())
                            .files(fileDTOS)
                            .creatorId(item.getUserId())
                            .pictures(pictureDTOS)
                            .replies(replyMore)
                            .build();
                }).toList());
        return response;
    }

    private ReplyDTO getReplyDTO(CourseStudentCommentPO reply) {
        Integer parentId = reply.getParentId();
        CourseStudentCommentPO parentComment = courseStudentCommentService.getById(parentId);
        Integer replyParentUserId = parentComment.getUserId();
        Integer replyParentUserType = parentComment.getUserType();
        String replyParentUserName;
        if (Objects.equals(replyParentUserType, 0)) {
            StudentPO student = studentService.getById(replyParentUserId);
            replyParentUserName = student == null ? "" : student.getStudentName();
        } else {
            UserPO user = userService.getById(replyParentUserId);
            replyParentUserName = user == null ? "" : user.getUserName();
        }

        String name;
        Integer userType = reply.getUserType();
        Integer userId = reply.getUserId();
        if (Objects.equals(userType, 0)) {
            StudentPO student = studentService.getById(userId);
            name = student == null ? "" : student.getStudentName();
        } else {
            UserPO user = userService.getById(userId);
            name = user == null ? "" : user.getUserName();
        }
        String files = reply.getFiles();
        List<FileDTO> fileDTOS = null;
        if (StringUtils.isNotBlank(files)) {
            fileDTOS = JSONUtil.toList(files, FileDTO.class);
            for (FileDTO fileDTO : fileDTOS) {
                fileDTO.setUrl(ossClientUtils.getSignedUrl(fileDTO.getUrl()));
            }
        }
        String pictures = reply.getPictures();
        List<FileDTO> pictureDTOS = null;
        if (StringUtils.isNotBlank(pictures)) {
            pictureDTOS = JSONUtil.toList(pictures, FileDTO.class);
            for (FileDTO fileDTO : pictureDTOS) {
                fileDTO.setUrl(ossClientUtils.getSignedUrl(fileDTO.getUrl()));
            }
        }
        log.info("id:{}, replyParentUserId:{}, replyParentUserName:{}", reply.getId(), replyParentUserId, replyParentUserName);
        return ReplyDTO.builder()
                .id(reply.getId())
                .userName(name)
                .userType(userType)
                .content(reply.getContent())
                .createTime(reply.getCreateTime())
                .files(fileDTOS)
                .pictures(pictureDTOS)
                .creatorId(reply.getUserId())
                .replyName(replyParentUserName)
                .replyUserType(replyParentUserType)
                .build();
    }

    public ReplyMoreDTO moreReplyList(Integer rootId, Integer after) {
        LambdaQueryWrapper<CourseStudentCommentPO> queryWrapper = Wrappers.lambdaQuery(CourseStudentCommentPO.class)
                .eq(CourseStudentCommentPO::getRootId, rootId)
                .gt(CourseStudentCommentPO::getParentId, 0)
                .gt(after != null, CourseStudentCommentPO::getId, after);
        long totalCount = courseStudentCommentService.count(queryWrapper);

        queryWrapper.orderByAsc(CourseStudentCommentPO::getId);
        queryWrapper.last("limit 10");
        List<CourseStudentCommentPO> replies = courseStudentCommentService.list(queryWrapper);
        List<ReplyDTO> replyDTOS = replies.parallelStream().map(this::getReplyDTO).toList();
        return ReplyMoreDTO.builder()
                .hasMore(totalCount > replies.size())
                .replies(replyDTOS)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean teacherDeleteComment(CommentDeleteCmd deleteCmd) {
        CourseStudentCommentPO comment = courseStudentCommentService.getById(deleteCmd.getId());
        if (comment == null) {
            return true;
        }
        if (Objects.equals(comment.getParentId(), 0)) {
            courseStudentCommentService.remove(Wrappers.lambdaQuery(CourseStudentCommentPO.class)
                    .eq(CourseStudentCommentPO::getRootId, deleteCmd.getId()));
        }
        return courseStudentCommentService.removeById(deleteCmd.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean studentDeleteComment(CommentDeleteCmd deleteCmd) {
        int studentId = StpUtil.getLoginIdAsInt();
        CourseStudentCommentPO comment = courseStudentCommentService.getById(deleteCmd.getId());
        if (!Objects.equals(comment.getUserId(), studentId)) {
            throw new BusinessException(ErrorCodeEnums.USER_NOT_AUTHORIZED);
        }
        if (Objects.equals(comment.getParentId(), 0)) {
            courseStudentCommentService.remove(Wrappers.lambdaQuery(CourseStudentCommentPO.class)
                    .eq(CourseStudentCommentPO::getRootId, deleteCmd.getId()));
        }
        return courseStudentCommentService.removeById(deleteCmd.getId());
    }
}
