package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.Receita;

import java.util.Comparator;
import java.util.List;

public record ReceitaResponse(
        Long id,
        String nome,
        String ingredientes,
        String modoPreparo,
        List<ErvaResumo> ervas
) {
    public static ReceitaResponse de(Receita r) {
        List<ErvaResumo> ervas = r.getErvas().stream()
                .sorted(Comparator.comparing(Erva::getId))
                .map(ErvaResumo::de)
                .toList();
        return new ReceitaResponse(r.getId(), r.getNome(), r.getIngredientes(), r.getModoPreparo(), ervas);
    }
}
