package fr.supmap.supmapapi.controller;

import fr.supmap.supmapapi.model.dto.RouteDto;
import fr.supmap.supmapapi.model.dto.userDtos.UserMinimalInfoDto;
import fr.supmap.supmapapi.model.dto.userDtos.UserUpdateDto;
import fr.supmap.supmapapi.model.entity.table.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * The interface User controller.
 *
 * @author Mathéo RIO (matheo.rio@supinfo.com)
 */
public interface UserController {

    /**
     * Get all users.
     *
     * @return a list of users
     */
    @GetMapping("/users")
    List<User> getAll();

    /**
     * Get a user info.
     *
     * @param userId the user id
     * @return the user
     */
    @GetMapping("/user/{userId}")
    UserMinimalInfoDto getUser(@PathVariable("userId") Integer userId);

    /**
     * Gets self user info (for the connected user).
     *
     * @return the user
     */
    @GetMapping("/user/info")
    UserMinimalInfoDto getUserInfo();

    /**
     * Update user.
     *
     * @param userUpdateDto the user update dto
     * @return the response dto
     */
    @PatchMapping("/user")
    UserMinimalInfoDto updateUser(@RequestBody UserUpdateDto userUpdateDto);

    /**
     * Gets user routes.
     *
     * @return the all routes of the user
     */
    @GetMapping("/user/allroutes")
    List<RouteDto> getAllRoutes();
}
