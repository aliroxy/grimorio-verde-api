package br.com.grimorioverde.assembler;

import br.com.grimorioverde.controller.v2.ApiKeyController;
import br.com.grimorioverde.dto.ApiKeyResponse;
import br.com.grimorioverde.security.ApiKey;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class ApiKeyAssembler implements RepresentationModelAssembler<ApiKey, EntityModel<ApiKeyResponse>> {

    @Override
    public EntityModel<ApiKeyResponse> toModel(ApiKey apiKey) {
        Link self = linkTo(ApiKeyController.class).slash(apiKey.getId()).withSelfRel();
        return EntityModel.of(ApiKeyResponse.de(apiKey),
                self,
                self.withRel("revoke"),
                linkTo(ApiKeyController.class).withRel("api-keys"));
    }
}
