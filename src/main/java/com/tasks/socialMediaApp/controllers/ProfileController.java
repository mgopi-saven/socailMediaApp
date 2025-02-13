package com.tasks.socialMediaApp.controllers;

import com.tasks.socialMediaApp.model.Profile;
import com.tasks.socialMediaApp.model.User;
import com.tasks.socialMediaApp.services.ProfileService;
import com.tasks.socialMediaApp.services.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/socialMediaApp/profile")
public class ProfileController {

    UserService userService;
    ProfileService profileService;

    ProfileController(UserService userService, ProfileService profileService){
        this.userService = userService;
        this.profileService = profileService;
    }

    @PutMapping("/editProfile")
    private ResponseEntity<?> editProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Profile profile){

        User user = userService.findUserByUserName(userDetails.getUsername());
        Profile savedProfile = profileService.handleEditProfile(user,profile);
        return ResponseEntity.ok(savedProfile);
    }

    @GetMapping("/getProfile")
    private ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails){

        User user = userService.findUserByUserName(userDetails.getUsername());
        Profile profile = profileService.fetchProfile(user);
        return ResponseEntity.ok(profile);
    }
}
