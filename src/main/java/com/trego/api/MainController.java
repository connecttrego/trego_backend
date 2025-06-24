package com.trego.api;

import com.trego.dto.MainDTO;
import com.trego.service.IMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @Autowired
    IMainService mainService;

    @GetMapping("/loadAll")
    public MainDTO loadAll(@RequestParam double lat, @RequestParam double lng) {
        return mainService.loadAll(lat, lng);
    }

}
