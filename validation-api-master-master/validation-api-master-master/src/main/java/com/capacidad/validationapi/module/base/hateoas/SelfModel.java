package com.capacidad.validationapi.module.base.hateoas;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.controller.BaseController;
import com.capacidad.validationapi.module.base.dto.BaseDTO;
import com.capacidad.validationapi.module.base.projection.BaseProjection;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.io.Serializable;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
public class SelfModel<P extends BaseProjection<I>, I extends Serializable> extends EntityModel<P> {

    public SelfModel(P projection, Class<? extends BaseController<? extends BaseDTO<I>, I>> controllerClass) throws ObjectNotFoundException, ObjectNotValidException {
        super(projection);
        if (controllerClass != null)
            add(linkTo(methodOn(controllerClass).getOne(projection.getId())).withSelfRel());
    }

    public SelfModel(P projection, Link... links) {
        super(projection);
        if (links != null)
            add(links);
    }

}