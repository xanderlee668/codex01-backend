package com.codex.backend.web;

import com.codex.backend.domain.user.User;
import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.service.DashboardService;
import com.codex.backend.web.dto.DashboardResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * iOS 首页聚合数据入口，提供单个接口返回 SampleData 中所需的所有模块。
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * 返回概览信息（统计、项目、今日/即将任务、快捷入口）。
     */
    @GetMapping
    public DashboardResponse getDashboard(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = principal.getUser();
        return dashboardService.buildDashboard(user);
    }
}
