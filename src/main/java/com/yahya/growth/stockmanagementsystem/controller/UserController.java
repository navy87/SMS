package com.yahya.growth.stockmanagementsystem.controller;

import com.google.common.collect.Lists;
import com.yahya.growth.stockmanagementsystem.model.security.Authority;
import com.yahya.growth.stockmanagementsystem.model.security.User;
import com.yahya.growth.stockmanagementsystem.security.Permission;
import com.yahya.growth.stockmanagementsystem.service.AuthorityService;
import com.yahya.growth.stockmanagementsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserController implements BasicControllerSkeleton<User>{

    private final UserService userService;
    private final AuthorityService authorityService;

    @Autowired
    public UserController(UserService userService, AuthorityService authorityService) {
        this.userService = userService;
        this.authorityService = authorityService;
    }

    @Override
    public String index(Model model) {
        return null;
    }

    @Override
    @GetMapping("")
    public String index(Model model, Principal principal) {
        model.addAttribute("users", userService.findAll());
        return "user/all";
    }

    @Override
    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        User user = userService.findById(id);
        System.out.println(user.getAuthorities());
        model.addAttribute("user", user);
        Set<String> authorities = Arrays.stream(Permission.values())
                .filter(permission -> !permission.getPermission().contains("report") && !permission.getPermission().contains("it:"))
                .map(permission -> permission.getPermission().split(":")[0])
                .collect(Collectors.toSet());
        model.addAttribute("currentAuthority", user.getAuthorities().stream().map(Authority::getAuthority).collect(Collectors.toSet()));
        model.addAttribute("allAuthorities", authorities);
        model.addAttribute("permissions", Lists.newArrayList());
        return "user/detail";
    }

    @PostMapping("/{id}")
    public String changePermissions(@PathVariable int id, @RequestParam("idPermission") List<String> permissions) {
        User user = userService.findById(id);
        user.getAuthorities().clear();
        permissions.stream()
                .map(authorityService::findByName)
                .forEach(user.getAuthorities()::add);
        userService.save(user);
        return "redirect:/users/" + id;
    }

    @Override
    @GetMapping("/new")
    public String addNewItem(Model model) {
        model.addAttribute("user", new User());
        return "user/signup";
    }

    @Override
    @PostMapping("/new")
    public String addNewPOST(User user) {
        user = userService.save(user);
        return "redirect:/users/" + user.getId();
    }

    @Override
    public String edit(int id, Model model) {
        throw new UnsupportedOperationException("Users can not be edited by other users.");
    }

    @Override
    public String editPost(int id, User obj) {
        throw new UnsupportedOperationException("Users can not be edited by other users.");
    }

//    @Override
//    @GetMapping("/edit")
//    public String edit(@RequestParam int id, Model model) {
//        model.addAttribute("user", userService.findById(id));
//        return "user/edit";
//    }
//
//    @Override
//    @PostMapping("/edit")
//    public String editPost(@RequestParam int id, User user) {
//        user.setId(id);
//        userService.save(user);
//        return "redirect:/users/" + user.getId();
//    }

    @Override
    @GetMapping("/delete")
    public String delete(@RequestParam int id) {
        userService.deleteById(id);
        return "redirect:/users";
    }
}
