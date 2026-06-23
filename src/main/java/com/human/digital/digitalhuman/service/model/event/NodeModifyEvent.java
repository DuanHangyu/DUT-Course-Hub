package com.human.digital.digitalhuman.service.model.event;

import com.human.digital.digitalhuman.repository.po.CourseNodePO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @USER taoHouChao
 * @DATE 12:51 2025/6/19
 */
@Getter
public class NodeModifyEvent extends ApplicationEvent {

    private final CourseNodePO courseNode;

    public NodeModifyEvent(Object source, CourseNodePO courseNode) {
        super(source);
        this.courseNode = courseNode;
    }
}
