package br.com.grimorioverde.assembler;

import br.com.grimorioverde.controller.v1.ErvaController;
import br.com.grimorioverde.controller.v1.FichaBotanicaController;
import br.com.grimorioverde.controller.v1.UsoController;
import br.com.grimorioverde.controller.v2.ErvaControllerV2;
import br.com.grimorioverde.dto.ErvaResponseV2;
import br.com.grimorioverde.entity.Erva;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ErvaAssemblerV2 implements RepresentationModelAssembler<Erva, EntityModel<ErvaResponseV2>> {

    @Override
    public EntityModel<ErvaResponseV2> toModel(Erva erva) {
        Long id = erva.getId();
        Link self = linkTo(ErvaControllerV2.class).slash(id).withSelfRel();

        List<Link> links = new ArrayList<>();
        links.add(self);
        links.add(self.withRel("update"));
        links.add(self.withRel("delete"));
        links.add(linkTo(ErvaControllerV2.class).withRel("ervas"));
        links.add(linkTo(UsoController.class).slash("erva").slash(id).withRel("usos"));
        if (erva.getFichaBotanica() != null) {
            links.add(linkTo(FichaBotanicaController.class).slash("erva").slash(id).withRel("ficha-botanica"));
        }
        links.add(linkTo(ErvaController.class).slash(id).withRel("versao-v1"));

        return EntityModel.of(ErvaResponseV2.de(erva), links);
    }
}
