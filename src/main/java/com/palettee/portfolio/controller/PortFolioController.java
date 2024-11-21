package com.palettee.portfolio.controller;

import com.palettee.portfolio.controller.dto.response.CustomSliceResponse;
import com.palettee.portfolio.controller.dto.response.PortFolioResponseDTO;
import com.palettee.portfolio.service.PortFolioService;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/portFolio")
@Slf4j
public class PortFolioController
{

    private final PortFolioService portFolioService;


    @GetMapping()
    public Slice<PortFolioResponseDTO>  findAll(
            Pageable pageable,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam MajorJobGroup majorJobGroup,
            @RequestParam MinorJobGroup minorJobGroup
            ){

        return portFolioService.findAllPortFolio(pageable,majorJobGroup, minorJobGroup,sort);
    }


    @GetMapping("/{portFolioId}")
    public void clickPortFolio(@PathVariable Long portFolioId){
        portFolioService.clickPortFolio(portFolioId);
    }

    @GetMapping("/my-page")
    public CustomSliceResponse findLike(Pageable pageable ,@RequestParam(required = false) Long likeId){
        return portFolioService.findListPortFolio(pageable,"k12002@nate.com", likeId);

    }
}
