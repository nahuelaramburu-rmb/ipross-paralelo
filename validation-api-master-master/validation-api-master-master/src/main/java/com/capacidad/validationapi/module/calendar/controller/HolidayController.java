package com.capacidad.validationapi.module.calendar.controller;

import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.constant.ControllerEndpoints;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.calendar.dto.HolidayDTO;
import com.capacidad.validationapi.module.calendar.projection.CalendarEventProjection;
import com.capacidad.validationapi.module.calendar.projection.HolidayProjection;
import com.capacidad.validationapi.module.calendar.service.CalendarEventService;
import com.capacidad.validationapi.module.calendar.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_HOLIDAYS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ControllerEndpoints.ENDPOINT_CALENDAR)
public class HolidayController extends BaseControllerImpl<HolidayDTO, Long> {

    private final HolidayService holidayService;
    private final CalendarEventService eventService;

    @Autowired
    public HolidayController(HolidayService holidayService, CalendarEventService eventService) {
        super(holidayService);
        this.holidayService = holidayService;
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<CollectionModelWrapper<EntityModel<HolidayProjection>>> findHolidaysByYearMonth(@RequestParam(required = false) Integer year,
                                                                                                          @RequestParam(required = false) Integer month) throws ObjectNotValidException {
        List<HolidayProjection> results = holidayService.find(year, month);
        ResourceAssembler<HolidayProjection, Long> assembler = new ResourceAssembler<>(this.getClass());
        Link self = linkTo(methodOn(this.getClass()).findHolidaysByYearMonth(year, month)).withSelfRel().expand();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_HOLIDAYS, assembler.toResources(results), self));
    }

    @GetMapping(path = "/events")
    public ResponseEntity<List<CalendarEventProjection>> getAllEvents() {
        return ResponseEntity.ok(this.eventService.findAllEvents());
    }

}
