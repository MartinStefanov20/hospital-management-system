package dev.mstefanov.hms.web;

import dev.mstefanov.hms.model.binding.RoleBindingModel;
import dev.mstefanov.hms.service.RoleService;
import dev.mstefanov.hms.service.UserService;
import dev.mstefanov.hms.utils.SessionUtils;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RoleController {

    private final UserService userService;
    private final RoleService roleService;
    private final SessionUtils sessionUtils;

    public RoleController(UserService userService, RoleService roleService, SessionUtils sessionUtils) {
        this.userService = userService;
        this.roleService = roleService;
        this.sessionUtils = sessionUtils;
    }

    @GetMapping("/admin/manage-roles-select-user")
    public String getRoleManager(Model model) {
        model.addAttribute("users", userService.getAllUsernames());
        return "admin/role-manager";
    }

    @PostMapping("/admin/manage-roles-select-user")
    public String manageRoles(@ModelAttribute("username") String username, RedirectAttributes redirectAttributes) {
        if (username == null || username.isBlank() || username.equals("Select User")) {
            redirectAttributes.addFlashAttribute("userNotSelected", "missing user");
            return "redirect:/admin/manage-roles-select-user";
        }
        redirectAttributes.addAttribute("username", username);
        return "redirect:/admin/role-manager";
    }

    @GetMapping("/admin/role-manager")
    public String getRoleManager(@RequestParam("username") String username, Model model) {
        List<String> roles = userService.getUserRoles(username);
        model.addAttribute("username", username);
        model.addAttribute("patient", roles.contains("ROLE_PATIENT"));
        model.addAttribute("doctor", roles.contains("ROLE_DOCTOR"));
        model.addAttribute("admin", roles.contains("ROLE_ADMIN"));
        return "admin/select-roles";
    }

    @PostMapping("/admin/role-manager")
    public String postRoleChanges(@Valid @ModelAttribute("roleBindingModel") RoleBindingModel roleBindingModel,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userNotSelected", "missing user");
            return "redirect:/admin/manage-roles-select-user";
        }
        if (roleBindingModel.isPatient() && roleBindingModel.isDoctor() && !roleBindingModel.isAdmin()) {
            redirectAttributes.addFlashAttribute("roleIssue",
                    "Can not be user and doctor at the same time without having admin rights!");
            redirectAttributes.addAttribute("username", roleBindingModel.getUsername());
            return "redirect:/admin/role-manager";
        }
        roleService.deleteCurrentRolesForUser(roleBindingModel.getUsername());
        userService.setUserWithNewRoles(roleBindingModel);
        sessionUtils.expireUserSessions(roleBindingModel.getUsername());
        return "redirect:/admin/manage-roles-select-user";
    }
}
