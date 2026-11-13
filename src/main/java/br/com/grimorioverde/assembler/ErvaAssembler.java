package br.com.grimorioverde.assembler;

import br.com.grimorioverde.controller.v1.ErvaController;
import br.com.grimorioverde.controller.v1.FichaBotanicaController;
import br.com.grimorioverde.controller.v1.ReceitaController;
import br.com.grimorioverde.controller.v1.RitualController;
import br.com.grimorioverde.controller.v1.UsoController;
import br.com.grimorioverde.controller.v2.ErvaControllerV2;
import br.com.grimorioverde.dto.ErvaResponse;
import br.com.grimorioverde.entity.Erva;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ErvaAssembler implements RepresentationModelAssembler<Erva, EntityModel<ErvaResponse>> {

    @Override
    public EntityModel<ErvaResponse> toModel(Erva erva) {
        Long id = erva.getId();
        Link self = linkTo(ErvaController.class).slash(id).withSelfRel();

        List<Link> links = new ArrayList<>();
        links.add(self);
        links.add(self.withRel("update"));
        links.add(self.withRel("delete"));
        links.add(linkTo(ErvaController.class).withRel("ervas"));
        links.add(linkTo(UsoController.class).slash("erva").slash(id).withRel("usos"));
        links.add(linkTo(ReceitaController.class).slash("erva").slash(id).withRel("receitas"));
        links.add(linkTo(RitualController.class).slash("erva").slash(id).withRel("rituais"));
        if (erva.getFichaBotanica() != null) {
            links.add(linkTo(FichaBotanicaController.class).slash("erva").slash(id).withRel("ficha-botanica"));
        }
        links.add(linkTo(ErvaControllerV2.class).slash(id).withRel("versao-v2"));

        return EntityModel.of(ErvaResponse.de(erva), links);
    }
}
