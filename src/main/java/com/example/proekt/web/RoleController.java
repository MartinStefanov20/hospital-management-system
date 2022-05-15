package com.example.proekt.web;

import com.example.proekt.model.binding.RoleBindingModel;
import com.example.proekt.service.RoleService;
import com.example.proekt.service.UserService;
import com.example.proekt.utils.SessionUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RoleController {

    private final UserService userService;
    private final RoleService roleService;
    private final ModelMapper modelMapper;
    private final SessionUtils sessionUtils;

    public RoleController(UserService userService, RoleService roleService, ModelMapper modelMapper, SessionUtils sessionUtils) {
        this.userService = userService;
        this.roleService = roleService;
        this.modelMapper = modelMapper;
        this.sessionUtils = sessionUtils;
    }

    @GetMapping("admin/manage-roles-select-user")
    public String getRoleManager(Model model) {

        model.addAttribute("users", this.userService.getAllUsernames());

        return "admin/role-manager";
    }

    @PostMapping("admin/manage-roles-select-user")
    public String manageRoles(@ModelAttribute("username") String username,
                              RedirectAttributes redirectAttributes) {

        if (username.equals("Select User")) {
            redirectAttributes.addFlashAttribute("userNotSelected", "missing user");
            return "redirect:manage-roles-select-user";
        }

        redirectAttributes.addAttribute("username", username);

        return "redirect:role-manager";
    }

    @GetMapping("admin/role-manager")
    public String getRoleManager(@RequestParam("username") String username,
                                 Model model) {

        boolean patient = false;
        boolean doctor = false;
        boolean admin = false;

        for (String userRole : this.userService.getUserRoles(username)) {
            if (userRole.equals("ROLE_PATIENT")) {
                patient = true;
            } else if (userRole.equals("ROLE_DOCTOR")) {
                doctor = true;
            } else if (userRole.equals("ROLE_ADMIN")) {
                admin = true;
            }
        }

        model.addAttribute("username", username);
        model.addAttribute("patient", patient);
        model.addAttribute("doctor", doctor);
        model.addAttribute("admin", admin);

        return "admin/select-roles";
    }

    @PostMapping("admin/role-manager")
    public String postRoleChanges(@ModelAttribute() RoleBindingModel roleBindingModel,
                                  RedirectAttributes redirectAttributes) {

        if (roleBindingModel.isPatient() && roleBindingModel.isDoctor()) {
            if (!roleBindingModel.isAdmin()) {
                redirectAttributes.addFlashAttribute("roleIssue",
                        "Can not be user and doctor at the same time without having admin rights!");
                redirectAttributes.addAttribute("username", roleBindingModel.getUsername());

                return "redirect:role-manager";
            }
        }

        this.roleService.deleteCurrentRolesForUser(roleBindingModel.getUsername());
        this.userService.setUserWithNewRoles(roleBindingModel);
        this.sessionUtils.expireUserSessions(roleBindingModel.getUsername());

        return "redirect:manage-roles-select-user";
    }
}
