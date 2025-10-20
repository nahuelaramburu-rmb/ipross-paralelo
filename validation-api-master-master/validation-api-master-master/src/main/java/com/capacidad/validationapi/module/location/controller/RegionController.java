package com.capacidad.validationapi.module.location.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.validationapi.module.base.controller.BaseControllerImpl;
import com.capacidad.validationapi.module.base.hateoas.CollectionModelWrapper;
import com.capacidad.validationapi.module.base.hateoas.ResourceAssembler;
import com.capacidad.validationapi.module.general.projection.IdAndNameOnlyProjection;
import com.capacidad.validationapi.module.location.dto.RegionDTO;
import com.capacidad.validationapi.module.location.projection.CountryProjection;
import com.capacidad.validationapi.module.location.projection.RegionProjection;
import com.capacidad.validationapi.module.location.service.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_REGIONS;
import static com.capacidad.validationapi.misc.constant.ResourceConstants.RESOURCE_REGIONS;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = ENDPOINT_REGIONS)
public class RegionController extends BaseControllerImpl<RegionDTO, Long> {

    private final RegionService regionService;

    @Autowired
    public RegionController(RegionService regionService) {
        super(regionService);
        this.regionService = regionService;
    }

    @GetMapping(params = "name")
    public ResponseEntity<CollectionModelWrapper<EntityModel<RegionProjection>>> getRegionsByName(@RequestParam String name) {
        Set<RegionProjection> results = regionService.getRegions(name);
        ResourceAssembler<RegionProjection, Long> assembler = new ResourceAssembler<>(this.getClass());
        Link self = linkTo(methodOn(this.getClass()).getRegionsByName(name)).withSelfRel();
        return ResponseEntity.ok(new CollectionModelWrapper<>(RESOURCE_REGIONS, assembler.toResources(results), self));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<RegionProjection>>> getAllRegions() {
        CollectionModel<EntityModel<RegionProjection>> resources = regionService.findAll();
        resources.add(linkTo(methodOn(this.getClass()).getAllRegions()).withSelfRel());
        return ResponseEntity.ok(resources);
    }

    @GetMapping(value = "/countries")
    public ResponseEntity<List<CountryProjection>> getCountries() {
        return ResponseEntity.ok(regionService.getAllCountries());
    }

    @GetMapping(value = "/countries/{countryId}")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getProvinces(@PathVariable Long countryId) throws ObjectNotFoundException {
        return ResponseEntity.ok(regionService.getProvinces(countryId));
    }

    @GetMapping(value = "/provinces/{provinceId}")
    public ResponseEntity<List<IdAndNameOnlyProjection>> getCities(@PathVariable Long provinceId) throws ObjectNotFoundException {
        return ResponseEntity.ok(regionService.getCities(provinceId));
    }

}
