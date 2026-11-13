package br.com.grimorioverde.assembler;

import br.com.grimorioverde.controller.v1.ErvaController;
import br.com.grimorioverde.controller.v1.UsoController;
import br.com.grimorioverde.dto.UsoResponse;
import br.com.grimorioverde.entity.Uso;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class UsoAssembler implements RepresentationModelAssembler<Uso, EntityModel<UsoResponse>> {

    @Override
    public EntityModel<UsoResponse> toModel(Uso uso) {
        Link self = linkTo(UsoController.class).slash(uso.getId()).withSelfRel();
        return EntityModel.of(UsoResponse.de(uso),
                self,
                self.withRel("update"),
                self.withRel("delete"),
                linkTo(UsoController.class).withRel("usos"),
                linkTo(ErvaController.class).slash(uso.getErva().getId()).withRel("erva"));
    }
}
