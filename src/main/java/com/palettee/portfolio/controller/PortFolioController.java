package com.palettee.portfolio.controller;

import com.palettee.global.security.validation.UserUtils;
import com.palettee.portfolio.controller.dto.response.CustomOffSetResponse;
import com.palettee.portfolio.controller.dto.response.CustomPortFolioResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioWrapper;
import com.palettee.portfolio.service.PortFolioService;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portFolio")
@Slf4j
public class PortFolioController
{

    private final PortFolioService portFolioService;


    @GetMapping()
    public CustomOffSetResponse findAll(
            Pageable pageable,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String majorJobGroup,
            @RequestParam(required = false) String minorJobGroup
            ){
        return portFolioService.findAllPortFolio(pageable,majorJobGroup, minorJobGroup,sort, getUserFromContext(),isFirst(pageable,majorJobGroup,minorJobGroup,sort));
    }

    @GetMapping("/{portFolioId}")
    public boolean clickPortFolio(@PathVariable Long portFolioId){
        return portFolioService.clickPortFolio(portFolioId, UserUtils.getContextUser().getId());
    }

    @GetMapping("/my-page")
    public CustomPortFolioResponse findLike(
            Pageable pageable ,
            @RequestParam(required = false) Long likeId){

        return portFolioService.findListPortFolio(pageable,UserUtils.getContextUser().getId(), likeId);

    }

    @PostMapping("/{portFolioId}/likes")
    public boolean createLikes(
            @PathVariable Long portFolioId
    ){
        return portFolioService.likePortFolio(UserUtils.getContextUser(), portFolioId);
    }

    @GetMapping("/main")
    public PortFolioWrapper findPopular(){

     return portFolioService.popularPortFolio(getUserFromContext());
    }


    private static boolean isFirst(Pageable pageable, String majorJobGroup, String minorJobGroup,String sort) {
        if(pageable.getOffset() == 0 && majorJobGroup == null && minorJobGroup == null && sort.equals("latest")){
            return true;
        }
        return false;
    }

    private Optional<User> getUserFromContext() {
        User user = null;
        try {
            user = UserUtils.getContextUser();
        } catch (Exception e) {
            log.info("Current user is not logged in");
        }
        return Optional.ofNullable(user);
    }

}
