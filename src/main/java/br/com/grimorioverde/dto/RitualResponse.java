package br.com.grimorioverde.dto;

import br.com.grimorioverde.entity.Erva;
import br.com.grimorioverde.entity.Ritual;

import java.util.Comparator;
import java.util.List;

public record RitualResponse(
        Long id,
        String nome,
        String finalidadeEspiritual,
        String instrucoes,
        String cuidados,
        List<ErvaResumo> ervas
) {
    public static RitualResponse de(Ritual r) {
        List<ErvaResumo> ervas = r.getErvas().stream()
                .sorted(Comparator.comparing(Erva::getId))
                .map(ErvaResumo::de)
                .toList();
        return new RitualResponse(r.getId(), r.getNome(), r.getFinalidadeEspiritual(),
                r.getInstrucoes(), r.getCuidados(), ervas);
    }
}
