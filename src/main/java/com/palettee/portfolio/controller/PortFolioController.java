package com.palettee.portfolio.controller;

import com.palettee.portfolio.controller.dto.PortFolioResponseDTO;
import com.palettee.portfolio.service.PortFolioService;
import com.palettee.user.domain.MajorJobGroup;
import com.palettee.user.domain.MinorJobGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestParam String sort,
            @RequestParam MajorJobGroup majorJobGroup,
            @RequestParam MinorJobGroup minorJobGroup
            ){
        log.info("findAll");

        return portFolioService.findAllPortFolio(pageable,majorJobGroup, minorJobGroup,sort);
    }
}
