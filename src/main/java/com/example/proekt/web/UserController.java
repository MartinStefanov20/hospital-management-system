package com.example.proekt.web;

import com.example.proekt.model.binding.UserRegistrationBindingModel;
import com.example.proekt.model.service.UserServiceModel;
import com.example.proekt.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }


    @PostMapping("/login-error")
    public ModelAndView onLoginError(
            @ModelAttribute(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY) String user) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.addObject("loginError", "bad_credentials");
        modelAndView.addObject("username", user);

        modelAndView.setViewName("login");

        return modelAndView;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {

        if (!model.containsAttribute("userRegistrationBindingModel")) {
            model.addAttribute("userRegistrationBindingModel", new UserRegistrationBindingModel());
        }

        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userRegistrationBindingModel")
                                   UserRegistrationBindingModel userRegistrationBindingModel,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            redirectAttributes.addFlashAttribute("userRegistrationBindingModel", userRegistrationBindingModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegistrationBindingModel",
                    bindingResult);

            if (!userRegistrationBindingModel.getPassword().equals(userRegistrationBindingModel.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("passwordsMismatch", "password mismatch");
            }

            redirectAttributes.addFlashAttribute("bindingIssues", "binding issues");

            return "redirect:register";

        } else {
            if (!userRegistrationBindingModel.getPassword().equals(userRegistrationBindingModel.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("passwordsMismatch", "password mismatch");
                redirectAttributes.addFlashAttribute("userRegistrationBindingModel", userRegistrationBindingModel);

                return "redirect:register";
            } else {
                if (this.userService.checkIfUsernameExists(userRegistrationBindingModel.getUsername())) {
                    redirectAttributes.addFlashAttribute("usernameIsTake", "taken username");
                    redirectAttributes.addFlashAttribute("userRegistrationBindingModel", userRegistrationBindingModel);

                    return "redirect:register";
                }
                this.userService.registerPatient(this.modelMapper.map(userRegistrationBindingModel, UserServiceModel.class));
            }
        }

        return "redirect:login";

    }

    @GetMapping("admin/admin-panel")
    public String getAdminPanel() {
        return "admin/admin-panel";
    }

}
