package com.codex.backend.config;

import com.codex.backend.domain.task.Task;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.TaskRepository;
import com.codex.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 预置任务数据，结构与前端 SampleData.swift 对齐，方便 iOS 客户端联调。
 */
@Component
public class SampleTaskInitializer {

    private static final Logger log = LoggerFactory.getLogger(SampleTaskInitializer.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public SampleTaskInitializer(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @PostConstruct
    @Transactional
    public void preloadTasks() {
        if (taskRepository.count() > 0) {
            log.debug("Skip sample tasks initialization because existing records were found");
            return;
        }

        userRepository.findAll().stream().findFirst().ifPresent(this::createSamplesForUser);
    }

    private void createSamplesForUser(User user) {
        LocalDate today = LocalDate.now();
        List<Task> samples = List.of(
                new Task(
                        user,
                        "设计入门流程",
                        "根据 UI 规范完善新手引导界面",
                        "Design",
                        "High",
                        today.plusDays(1),
                        90,
                        List.of("SwiftUI", "Onboarding"),
                        false),
                new Task(
                        user,
                        "实现同步接口",
                        "补全后台同步任务状态的 API",
                        "Development",
                        "High",
                        today.plusDays(3),
                        120,
                        List.of("API", "Backend"),
                        false),
                new Task(
                        user,
                        "撰写六月推送文案",
                        "准备营销活动的推送内容",
                        "Marketing",
                        "Medium",
                        today.plusDays(2),
                        45,
                        List.of("Campaign", "Copy"),
                        false),
                new Task(
                        user,
                        "回顾 OKR",
                        "梳理本周个人目标完成度",
                        "Personal",
                        "Low",
                        today,
                        30,
                        List.of("Review"),
                        true));

        samples.forEach(taskRepository::save);
        log.info("✅ 已插入 {} 条示例任务数据，方便前端联调", samples.size());
    }
}
