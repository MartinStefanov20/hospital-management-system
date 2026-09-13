package dev.mstefanov.hms.web;

import dev.mstefanov.hms.model.binding.UserRegistrationBindingModel;
import dev.mstefanov.hms.model.service.UserServiceModel;
import dev.mstefanov.hms.service.UserService;
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

import jakarta.validation.Valid;

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
        if (!userRegistrationBindingModel.passwordsMatch()) {
            redirectAttributes.addFlashAttribute("passwordsMismatch", "password mismatch");
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("bindingIssues", "binding issues");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userRegistrationBindingModel",
                    bindingResult);
        }
        if (bindingResult.hasErrors() || !userRegistrationBindingModel.passwordsMatch()) {
            redirectAttributes.addFlashAttribute("userRegistrationBindingModel", userRegistrationBindingModel);
            return "redirect:/users/register";
        }
        if (this.userService.checkIfUsernameExists(userRegistrationBindingModel.getUsername())) {
            redirectAttributes.addFlashAttribute("usernameIsTake", "taken username");
            redirectAttributes.addFlashAttribute("userRegistrationBindingModel", userRegistrationBindingModel);
            return "redirect:/users/register";
        }
        this.userService.registerPatient(this.modelMapper.map(userRegistrationBindingModel, UserServiceModel.class));
        return "redirect:/users/login";
    }

    @GetMapping("/admin/panel")
    public String getAdminPanel() {
        return "admin/panel";
    }

}
