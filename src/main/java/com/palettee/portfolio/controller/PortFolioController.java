package com.palettee.portfolio.controller;

import com.amazonaws.services.kms.model.NotFoundException;
import com.palettee.global.security.oauth.CustomOAuth2User;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioLikeResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.service.PortFolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portFolio")
@Slf4j
public class PortFolioController
{

    private final PortFolioService portFolioService;


    @GetMapping()
    public Slice<PortFolioResponse>  findAll(
            Pageable pageable,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam String majorJobGroup,
            @RequestParam String minorJobGroup
            ){

        return portFolioService.findAllPortFolio(pageable,majorJobGroup, minorJobGroup,sort);
    }


    @GetMapping("/{portFolioId}")
    public void clickPortFolio(@PathVariable Long portFolioId){
        portFolioService.clickPortFolio(portFolioId);
    }

    @GetMapping("/my-page")
    public CustomSliceResponse findLike(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            Pageable pageable ,
            @RequestParam(required = false) Long likeId){

        validationContexts(customOAuth2User);
        return portFolioService.findListPortFolio(pageable,customOAuth2User.getUser().getId(), likeId);

    }

    @PostMapping("/likes")
    public PortFolioLikeResponse createLikes(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @RequestParam Long portFolioId
    ){
        validationContexts(customOAuth2User);

        return portFolioService.createPortFolioLike(portFolioId, customOAuth2User.getUser());
    }

    

    private static void validationContexts(CustomOAuth2User customOAuth2User) {
        if(customOAuth2User.getUser() == null) {
            throw new NotFoundException("User not found");
        }
    }



}
