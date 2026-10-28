package com.energymonitor.home.adapter.in.internal;

import com.energymonitor.home.application.port.in.DescribeHome;
import com.energymonitor.home.application.port.in.DescribeHome.HomeSummary;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Asked by energy-monitor-backend for a home and its members (the notifications write to every
 * member): 200 with {@code {"homeId":..,"name":..,"memberIds":[..],...}}, or 404 when the home
 * does not exist. Only the services reach it; see {@code InternalApiConfiguration}.
 */
@Hidden
@RestController
public class HomeSummaryController {

    private final DescribeHome describeHome;

    public HomeSummaryController(DescribeHome describeHome) {
        this.describeHome = describeHome;
    }

    @GetMapping("/internal/homes/{homeId}")
    public ResponseEntity<HomeSummary> home(@PathVariable String homeId) {
        return ResponseEntity.of(describeHome.describe(homeId));
    }
}
