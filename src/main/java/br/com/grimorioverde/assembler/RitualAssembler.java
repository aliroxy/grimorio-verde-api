package br.com.grimorioverde.assembler;

import br.com.grimorioverde.controller.v1.ErvaController;
import br.com.grimorioverde.controller.v1.RitualController;
import br.com.grimorioverde.dto.RitualResponse;
import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.Ritual;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class RitualAssembler implements RepresentationModelAssembler<Ritual, EntityModel<RitualResponse>> {

    @Override
    public EntityModel<RitualResponse> toModel(Ritual ritual) {
        Link self = linkTo(RitualController.class).slash(ritual.getId()).withSelfRel();

        List<Link> links = new ArrayList<>();
        links.add(self);
        links.add(self.withRel("update"));
        links.add(self.withRel("delete"));
        links.add(linkTo(RitualController.class).withRel("rituais"));
        ritual.getErvas().stream()
                .sorted(Comparator.comparing(Erva::getId))
                .forEach(e -> links.add(linkTo(ErvaController.class).slash(e.getId()).withRel("erva")));

        return EntityModel.of(RitualResponse.de(ritual), links);
    }
}
