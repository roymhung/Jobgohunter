package vn.proy.jobgohunter.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.proy.jobgohunter.domain.Permission;
import vn.proy.jobgohunter.domain.Role;
import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.service.UserService;
import vn.proy.jobgohunter.util.SecurityUtil;
import vn.proy.jobgohunter.util.error.IdInvalidException;

public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String requestURI = request.getRequestURI();
        String httpMethod = request.getMethod();
        System.out.println(">>> RUN preHandle");
        System.out.println(">>> path= " + path);
        System.out.println(">>> httpMethod= " + httpMethod);
        System.out.println(">>> requestURI= " + requestURI);

        String email = SecurityUtil.getCurrentUserLogin().isPresent() == true
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email != null && !email.isEmpty()) {
            User user = this.userService.handleGetUserByUsername(email);
            if (user != null) {
                Role role = user.getRole();
                if (role != null) {
                    List<Permission> permissions = role.getPermissions();
                    boolean isAllow =
                            permissions.stream().anyMatch(item -> item.getApiPath().equals(path)
                                    && item.getMethod().equals(httpMethod));
                    if (isAllow == false) {
                        throw new IdInvalidException("Bạn không có quyền truy cập vào API này");
                    }
                } else {
                    throw new IdInvalidException("Bạn không có quyền truy cập vào API này");
                }
            }
        }
        return true;
    }
}
