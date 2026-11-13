package br.com.grimorioverde.assembler;

import br.com.grimorioverde.controller.v1.ErvaController;
import br.com.grimorioverde.controller.v1.FichaBotanicaController;
import br.com.grimorioverde.dto.FichaBotanicaResponse;
import br.com.grimorioverde.entity.FichaBotanica;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class FichaBotanicaAssembler
        implements RepresentationModelAssembler<FichaBotanica, EntityModel<FichaBotanicaResponse>> {

    @Override
    public EntityModel<FichaBotanicaResponse> toModel(FichaBotanica ficha) {
        Link self = linkTo(FichaBotanicaController.class).slash(ficha.getId()).withSelfRel();
        return EntityModel.of(FichaBotanicaResponse.de(ficha),
                self,
                self.withRel("update"),
                self.withRel("delete"),
                linkTo(FichaBotanicaController.class).withRel("fichas-botanicas"),
                linkTo(ErvaController.class).slash(ficha.getErva().getId()).withRel("erva"));
    }
}
