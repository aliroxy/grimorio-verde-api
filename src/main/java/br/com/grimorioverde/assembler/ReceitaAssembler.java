package br.com.grimorioverde.assembler;

import br.com.grimorioverde.controller.v1.ErvaController;
import br.com.grimorioverde.controller.v1.ReceitaController;
import br.com.grimorioverde.dto.ReceitaResponse;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.Receita;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ReceitaAssembler implements RepresentationModelAssembler<Receita, EntityModel<ReceitaResponse>> {

    @Override
    public EntityModel<ReceitaResponse> toModel(Receita receita) {
        Link self = linkTo(ReceitaController.class).slash(receita.getId()).withSelfRel();

        List<Link> links = new ArrayList<>();
        links.add(self);
        links.add(self.withRel("update"));
        links.add(self.withRel("delete"));
        links.add(linkTo(ReceitaController.class).withRel("receitas"));
        receita.getErvas().stream()
                .sorted(Comparator.comparing(Erva::getId))
                .forEach(e -> links.add(linkTo(ErvaController.class).slash(e.getId()).withRel("erva")));

        return EntityModel.of(ReceitaResponse.de(receita), links);
    }
}
