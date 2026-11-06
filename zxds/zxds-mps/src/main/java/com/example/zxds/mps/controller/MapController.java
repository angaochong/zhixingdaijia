package com.example.zxds.mps.controller;

import com.example.zxds.common.util.R;
import com.example.zxds.mps.controller.form.CalculateDriveLineForm;
import com.example.zxds.mps.controller.form.EstimateOrderMileageAndMinuteForm;
import com.example.zxds.mps.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
@Tag(name = "MapController", description = "地图Web接口")
public class MapController {

    private final MapService mapService;

    @PostMapping("/estimateOrderMileageAndMinute")
    @Operation(summary = "估算里程和时间")
    public R estimateOrderMileageAndMinute(@RequestBody @Valid EstimateOrderMileageAndMinuteForm form) {
        Map<String, Object> map = mapService.estimateOrderMileageAnfMinute(
                form.getMode(),
                form.getStartPlaceLatitude(),
                form.getStartPlaceLongitude(),
                form.getEndPlaceLatitude(),
                form.getEndPlaceLongitude());
        return R.ok().put("result", map);
    }

    @PostMapping("/calculateDriveLine")
    @Operation(summary = "计算行驶路线")
    public R calculateDriverLine(@RequestBody @Valid CalculateDriveLineForm form){
        Map<String, Object> map = mapService.calculateDriverLine(
                form.getStartPlaceLatitude(),
                form.getStartPlaceLongitude(),
                form.getEndPlaceLatitude(),
                form.getEndPlaceLongitude());
        return R.ok().put("result", map);
    }

}
