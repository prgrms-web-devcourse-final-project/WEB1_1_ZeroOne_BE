package com.palettee.portfolio.controller;

import com.palettee.global.security.validation.UserUtils;
import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioWrapper;
import com.palettee.portfolio.service.PortFolioService;
import com.palettee.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestParam(required = false) String majorJobGroup,
            @RequestParam(required = false) String minorJobGroup
            ){

        return portFolioService.findAllPortFolio(pageable,majorJobGroup, minorJobGroup,sort);
    }


    @GetMapping("/{portFolioId}")
    public void clickPortFolio(@PathVariable Long portFolioId){
        portFolioService.clickPortFolio(portFolioId, UserUtils.getContextUser().getId());
    }

    @GetMapping("/my-page")
    public CustomSliceResponse findLike(
            Pageable pageable ,
            @RequestParam(required = false) Long likeId){

        User contextUser = UserUtils.getContextUser();

        return portFolioService.findListPortFolio(pageable,contextUser.getId(), likeId);

    }

    @PostMapping("/{portFolioId}/likes")
    public boolean createLikes(
            @PathVariable Long portFolioId
    ){
        return portFolioService.likePortFolio(UserUtils.getContextUser(), portFolioId);
    }

    @GetMapping("/main")
    public PortFolioWrapper findPopular(){

     return portFolioService.popularPortFolio();
    }




}
