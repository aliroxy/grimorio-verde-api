package br.com.grimorioverde.config;

import br.com.grimorioverde.entity.*;
import br.com.grimorioverde.repository.*;
import br.com.grimorioverde.security.ApiKeyService;
import br.com.grimorioverde.security.NivelAcesso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Carrega dados de exemplo e a chave ADMIN de desenvolvimento ao iniciar a aplicação.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ErvaRepository ervaRepository;
    private final FichaBotanicaRepository fichaRepository;
    private final UsoRepository usoRepository;
    private final ReceitaRepository receitaRepository;
    private final RitualRepository ritualRepository;
    private final ApiKeyService apiKeyService;
    private final String chaveAdmin;
    private final String chaveUsuario;

    public DataInitializer(ErvaRepository ervaRepository,
                           FichaBotanicaRepository fichaRepository,
                           UsoRepository usoRepository,
                           ReceitaRepository receitaRepository,
                           RitualRepository ritualRepository,
                           ApiKeyService apiKeyService,
                           @Value("${grimorio.security.admin-api-key}") String chaveAdmin,
                           @Value("${grimorio.security.user-api-key}") String chaveUsuario) {
        this.ervaRepository = ervaRepository;
        this.fichaRepository = fichaRepository;
        this.usoRepository = usoRepository;
        this.receitaRepository = receitaRepository;
        this.ritualRepository = ritualRepository;
        this.apiKeyService = apiKeyService;
        this.chaveAdmin = chaveAdmin;
        this.chaveUsuario = chaveUsuario;
    }

    @Override
    public void run(String... args) {
        apiKeyService.garantirChave("Administrador (desenvolvimento)", NivelAcesso.ADMIN, chaveAdmin);
        apiKeyService.garantirChave("Usuário (desenvolvimento)", NivelAcesso.USUARIO, chaveUsuario);
        log.info("Chaves de desenvolvimento ativas (cabeçalho X-API-Key). ADMIN: {} | USUARIO: {}", chaveAdmin, chaveUsuario);

        if (ervaRepository.count() > 0) {
            return;
        }

        Erva camomila = erva("Camomila", "Matricaria chamomilla", CategoriaErva.MEDICINAL, "Europa",
                "Erva aromática de flores brancas com centro amarelo, tradicionalmente consumida em infusão.");
        Erva alecrim = erva("Alecrim", "Salvia rosmarinus", CategoriaErva.CULINARIA, "Região do Mediterrâneo",
                "Arbusto aromático de folhas finas e perfumadas, muito usado como tempero e em defumações.");
        Erva hortela = erva("Hortelã", "Mentha spicata", CategoriaErva.AROMATICA, "Europa e Ásia",
                "Planta rasteira de folhas aromáticas e sabor refrescante, usada em chás, sucos e sobremesas.");
        Erva manjericao = erva("Manjericão", "Ocimum basilicum", CategoriaErva.CULINARIA, "Sul da Ásia",
                "Erva de folhas macias e aroma marcante, ingrediente clássico de molhos e saladas.");

        ficha(camomila, "Flores", "Sol pleno ou meia-sombra, solo bem drenado e regas moderadas.",
                "Planta anual de até 60 cm, com folhas finas e flores perfumadas em forma de margarida.",
                "Pessoas alérgicas a plantas da família das Asteráceas devem evitar o consumo.");
        ficha(alecrim, "Folhas e ramos", "Sol pleno, solo arenoso e pouca água.",
                "Arbusto perene lenhoso, de folhas estreitas e flores azul-claras.", null);
        ficha(hortela, "Folhas", "Solo úmido, meia-sombra e cultivo em vaso para conter o crescimento.",
                "Erva perene de caule rasteiro, que se espalha com facilidade por estolões.", null);

        uso(camomila, TipoUso.MEDICINAL,
                "Tradicionalmente consumida em infusão para momentos de relaxamento.",
                "Infusão de 1 colher de sopa de flores secas em 200 ml de água quente por 5 minutos.",
                "Gestantes, lactantes e pessoas com alergias devem consultar um profissional de saúde antes de usar.");
        uso(camomila, TipoUso.CULINARIO,
                "Usada em chás e como aromatizante em doces e licores.",
                "Infusão das flores secas em água ou leite quente.", null);
        uso(alecrim, TipoUso.CULINARIO,
                "Tempero para carnes, batatas, pães e azeites aromatizados.",
                "Use os ramos frescos inteiros ou as folhas picadas durante o cozimento.", null);
        uso(alecrim, TipoUso.ESPIRITUAL,
                "Associado, em tradições populares, à proteção e à limpeza simbólica de ambientes.",
                null, "Mantenha longe de materiais inflamáveis e de crianças ao queimar.");
        uso(hortela, TipoUso.CULINARIO,
                "Dá frescor a chás, sucos, saladas e sobremesas.",
                "Use as folhas frescas inteiras ou levemente amassadas.", null);

        Receita cha = receita("Chá de camomila com hortelã",
                "1 colher de sopa de camomila seca; 4 folhas de hortelã; 200 ml de água",
                "Despeje a água quente sobre as ervas, tampe e aguarde 5 minutos. Coe e sirva morno.",
                camomila, hortela);
        Receita batatas = receita("Batatas assadas com alecrim",
                "500 g de batatas; 3 ramos de alecrim; 3 colheres de sopa de azeite; sal a gosto",
                "Corte as batatas, misture com o azeite, o sal e o alecrim e asse a 200 °C por cerca de 40 minutos.",
                alecrim);
        log.info("Receitas de exemplo criadas: {} e {}", cha.getNome(), batatas.getNome());

        ritual("Defumação de alecrim",
                "Simbolizar a limpeza do ambiente e a renovação das energias da casa.",
                "Queime um galho seco de alecrim em recipiente à prova de fogo, com a janela aberta, "
                        + "e percorra os cômodos com cuidado.",
                "Mantenha longe de crianças e de materiais inflamáveis; nunca deixe a brasa sem supervisão.",
                alecrim);
        ritual("Banho de ervas aromáticas",
                "Marcar um momento de pausa e autocuidado de forma simbólica.",
                "Prepare uma infusão de hortelã e camomila, deixe amornar e use como último enxágue.",
                "Teste a infusão em uma pequena área da pele antes e evite em caso de alergia.",
                hortela, camomila);

        ervasAdicionais();

        log.info("Dados de exemplo carregados: {} ervas.", ervaRepository.count());
    }


    /** Ervas adicionais, cada uma com ficha botânica e um uso, para chegar a 20 ervas de exemplo. */
    private void ervasAdicionais() {
        Map<String, Erva> ervas = new HashMap<>();
        ervas.put("Lavanda", completa("Lavanda", "Lavandula angustifolia", CategoriaErva.AROMATICA, "Região do Mediterrâneo",
                "Arbusto de flores roxas muito perfumadas, usado em sachês, óleos aromáticos e chás.",
                "Flores", "Sol pleno, solo arenoso e bem drenado, pouca rega.",
                "Arbusto perene de até 80 cm, com folhas estreitas acinzentadas e espigas de flores lilás.", null,
                TipoUso.ESPIRITUAL, "Associada, em tradições populares, ao relaxamento e à harmonização de ambientes.",
                "Sachê de flores secas dentro de gavetas ou perto da cama.", null));
        ervas.put("Hortelã-pimenta", completa("Hortelã-pimenta", "Mentha x piperita", CategoriaErva.MEDICINAL, "Europa",
                "Híbrido de hortelã de sabor intenso e refrescante, rico em mentol.",
                "Folhas", "Solo úmido e rico em matéria orgânica, meia-sombra.",
                "Erva perene de caule arroxeado e folhas dentadas com forte aroma de mentol.", "Evitar em crianças pequenas e em pessoas com refluxo.",
                TipoUso.MEDICINAL, "Tradicionalmente consumida em infusão após as refeições.",
                "Infusão de 1 colher de sopa de folhas em 200 ml de água quente por 5 minutos.", "Informação educativa, sem substituir orientação médica. Gestantes, lactantes, crianças e pessoas que usam medicamentos devem consultar um profissional de saúde antes de usar."));
        ervas.put("Erva-cidreira", completa("Erva-cidreira", "Melissa officinalis", CategoriaErva.MEDICINAL, "Europa e Ásia Ocidental",
                "Erva de folhas com aroma cítrico suave, tradicionalmente usada em chás calmantes.",
                "Folhas", "Meia-sombra, solo fértil e úmido.",
                "Planta perene de folhas ovais e levemente rugosas, com cheiro de limão.", null,
                TipoUso.MEDICINAL, "Consumida em infusão em tradições populares para momentos de relaxamento.",
                "Infusão de 1 colher de sopa de folhas frescas em 200 ml de água quente por 5 minutos.", "Informação educativa, sem substituir orientação médica. Gestantes, lactantes, crianças e pessoas que usam medicamentos devem consultar um profissional de saúde antes de usar."));
        ervas.put("Erva-doce", completa("Erva-doce", "Pimpinella anisum", CategoriaErva.CULINARIA, "Mediterrâneo e Oriente Médio",
                "Planta de sementes aromáticas com sabor adocicado de anis, usada em chás e panificação.",
                "Sementes", "Sol pleno, solo leve e bem drenado.",
                "Planta anual de até 50 cm com pequenas flores brancas reunidas em umbelas.", "Pode causar reação em pessoas alérgicas a plantas da família das Apiáceas.",
                TipoUso.CULINARIO, "Sementes usadas em pães, biscoitos, licores e chás.",
                "Adicionar as sementes à massa ou fazer infusão com 1 colher de chá em 200 ml de água.", null));
        ervas.put("Boldo", completa("Boldo", "Plectranthus barbatus", CategoriaErva.MEDICINAL, "África e Ásia tropical",
                "Arbusto de folhas aveludadas e amargas, muito presente em chás populares no Brasil.",
                "Folhas", "Sol pleno ou meia-sombra, solo bem drenado.",
                "Arbusto de folhas grandes, macias e aromáticas, que cresce com facilidade em vasos.", "Não usar por longos períodos nem sem orientação profissional.",
                TipoUso.MEDICINAL, "Tradicionalmente consumido em infusão após refeições pesadas.",
                "Infusão de 1 folha em 150 ml de água quente por 5 minutos.", "Informação educativa, sem substituir orientação médica. Gestantes, lactantes, crianças e pessoas que usam medicamentos devem consultar um profissional de saúde antes de usar."));
        ervas.put("Sálvia", completa("Sálvia", "Salvia officinalis", CategoriaErva.CULINARIA, "Região do Mediterrâneo",
                "Arbusto de folhas acinzentadas e aroma intenso, usado em pratos com carnes e massas.",
                "Folhas", "Sol pleno, solo seco e bem drenado.",
                "Subarbusto perene de folhas aveludadas verde-acinzentadas e flores azul-violeta.", "Evitar o consumo em grandes quantidades.",
                TipoUso.CULINARIO, "Folhas frescas douradas na manteiga para acompanhar massas e carnes.",
                "Aqueça a manteiga e doure as folhas por cerca de 1 minuto.", null));
        ervas.put("Tomilho", completa("Tomilho", "Thymus vulgaris", CategoriaErva.CULINARIA, "Região do Mediterrâneo",
                "Erva de pequenas folhas aromáticas, base de ervas finas e de caldos.",
                "Folhas e ramos", "Sol pleno, solo pobre e bem drenado, rega moderada.",
                "Subarbusto baixo de folhas minúsculas e flores rosadas ou brancas.", null,
                TipoUso.CULINARIO, "Tempero para caldos, assados, molhos e legumes.",
                "Adicione os ramos inteiros no início do cozimento e retire-os ao servir.", null));
        ervas.put("Orégano", completa("Orégano", "Origanum vulgare", CategoriaErva.CULINARIA, "Europa e Ásia",
                "Erva de sabor marcante, indispensável em pizzas, molhos de tomate e saladas.",
                "Folhas", "Sol pleno, solo bem drenado e rega moderada.",
                "Planta perene de até 60 cm, folhas ovais e pequenas flores rosadas.", null,
                TipoUso.CULINARIO, "Tempero seco ou fresco para pizzas, molhos e carnes grelhadas.",
                "Polvilhar sobre o prato no final do preparo para preservar o aroma.", null));
        ervas.put("Salsinha", completa("Salsinha", "Petroselinum crispum", CategoriaErva.CULINARIA, "Região do Mediterrâneo",
                "Erva de folhas verdes e frescas, uma das mais usadas como tempero no dia a dia.",
                "Folhas", "Sol pleno ou meia-sombra, solo fértil e úmido.",
                "Planta bienal de folhas recortadas e caules finos.", null,
                TipoUso.CULINARIO, "Cheiro-verde para arroz, feijão, molhos, saladas e recheios.",
                "Picar as folhas frescas e adicionar no final do cozimento.", null));
        ervas.put("Coentro", completa("Coentro", "Coriandrum sativum", CategoriaErva.CULINARIA, "Mediterrâneo e Ásia Ocidental",
                "Erva de folhas e sementes aromáticas, muito usada em culinárias do Nordeste, da Ásia e do México.",
                "Folhas e sementes", "Sol pleno, solo leve e semeadura direta.",
                "Planta anual de folhas recortadas e flores brancas pequenas.", "Algumas pessoas percebem sabor de sabão nas folhas.",
                TipoUso.CULINARIO, "Folhas frescas em moquecas, salsas e saladas; sementes em curries e conservas.",
                "Picar as folhas frescas e adicionar ao final do cozimento.", null));
        ervas.put("Gengibre", completa("Gengibre", "Zingiber officinale", CategoriaErva.MEDICINAL, "Sul da Ásia",
                "Rizoma picante e aromático, usado como tempero e em chás tradicionais.",
                "Rizoma", "Meia-sombra, solo rico e úmido, clima quente.",
                "Planta herbácea de rizoma nodoso e folhas lanceoladas.", "Evitar em excesso; pessoas com problemas de coagulação devem consultar um profissional.",
                TipoUso.MEDICINAL, "Tradicionalmente consumido em chás e infusões.",
                "Ferva 2 fatias finas do rizoma em 200 ml de água por 5 minutos.", "Informação educativa, sem substituir orientação médica. Gestantes, lactantes, crianças e pessoas que usam medicamentos devem consultar um profissional de saúde antes de usar."));
        ervas.put("Cúrcuma", completa("Cúrcuma", "Curcuma longa", CategoriaErva.CULINARIA, "Sul da Ásia",
                "Rizoma de cor alaranjada intensa, base do açafrão-da-terra e de muitos curries.",
                "Rizoma", "Meia-sombra, solo fértil, úmido e clima quente.",
                "Planta herbácea de folhas grandes e rizoma alaranjado.", "Pode manchar utensílios e tecidos.",
                TipoUso.CULINARIO, "Tempero em arroz, caldos, curries e molhos.",
                "Adicione o pó ou o rizoma ralado no início do preparo.", null));
        ervas.put("Capim-limão", completa("Capim-limão", "Cymbopogon citratus", CategoriaErva.MEDICINAL, "Sul da Ásia",
                "Gramínea de folhas longas com intenso aroma cítrico, usada em chás e temperos.",
                "Folhas", "Sol pleno, solo fértil e rega frequente.",
                "Touceira de folhas longas, finas e cortantes nas bordas, com cheiro de limão.", "Cuidado ao manusear: as bordas das folhas podem cortar.",
                TipoUso.MEDICINAL, "Tradicionalmente consumido em infusão em tradições populares.",
                "Infusão de 1 colher de sopa de folhas picadas em 200 ml de água quente por 5 minutos.", "Informação educativa, sem substituir orientação médica. Gestantes, lactantes, crianças e pessoas que usam medicamentos devem consultar um profissional de saúde antes de usar."));
        ervas.put("Calêndula", completa("Calêndula", "Calendula officinalis", CategoriaErva.ORNAMENTAL, "Europa",
                "Planta de flores alaranjadas ou amarelas, usada como ornamento e em preparos caseiros.",
                "Flores", "Sol pleno, solo bem drenado.",
                "Planta anual de até 50 cm, com flores vistosas em forma de margarida.", "Pessoas alérgicas a Asteráceas devem evitar.",
                TipoUso.CULINARIO, "Pétalas comestíveis para decorar saladas, arroz e bolos.",
                "Retirar as pétalas e espalhar sobre o prato pronto.", null));
        ervas.put("Arruda", completa("Arruda", "Ruta graveolens", CategoriaErva.ESPIRITUAL, "Região do Mediterrâneo",
                "Arbusto de forte cheiro, presente em tradições populares de proteção e usado como ornamental.",
                "Ramos (somente uso ornamental e simbólico)", "Sol pleno, solo bem drenado e pouca rega.",
                "Subarbusto de folhas azuladas recortadas e flores amarelas.", "Tóxica: não deve ser ingerida; o contato com a seiva pode causar queimaduras na pele sob o sol.",
                TipoUso.ESPIRITUAL, "Mantida em vasos, em tradições populares, como símbolo de proteção do lar.",
                "Cultivar em vaso na entrada da casa e manusear com luvas.", "Não ingerir. Gestantes devem evitar o contato; use luvas ao podar."));
        ervas.put("Cebolinha", completa("Cebolinha", "Allium schoenoprasum", CategoriaErva.CULINARIA, "Europa, Ásia e América do Norte",
                "Erva de folhas tubulares com sabor suave de cebola, parte do tradicional cheiro-verde.",
                "Folhas", "Sol pleno, solo fértil e úmido.",
                "Planta perene em touceira, folhas cilíndricas e flores lilás em formato de bolinha.", null,
                TipoUso.CULINARIO, "Finalização de sopas, omeletes, batatas, molhos e saladas.",
                "Picar as folhas frescas e adicionar logo antes de servir.", null));
        receitasERituaisAdicionais(ervas);
    }

    private void receitasERituaisAdicionais(Map<String, Erva> e) {
        receita("Chá calmante de erva-cidreira e capim-limão",
                "1 colher de sopa de folhas de erva-cidreira; 1 colher de sopa de capim-limão picado; 300 ml de água",
                "Ferva a água, desligue o fogo, adicione as ervas, tampe e aguarde 5 minutos. Coe e sirva morno.",
                e.get("Erva-cidreira"), e.get("Capim-limão"));
        receita("Chá de boldo com erva-doce após as refeições",
                "1 folha de boldo; 1 colher de chá de sementes de erva-doce; 200 ml de água",
                "Despeje a água quente sobre o boldo e a erva-doce, tampe por 5 minutos e coe. Consuma morno e sem exageros.",
                e.get("Boldo"), e.get("Erva-doce"));
        receita("Chá de gengibre com hortelã-pimenta",
                "2 fatias finas de gengibre; 5 folhas de hortelã-pimenta; 250 ml de água; mel a gosto",
                "Ferva o gengibre na água por 5 minutos, apague o fogo, junte a hortelã-pimenta e tampe por 3 minutos. Coe e adoce se quiser.",
                e.get("Gengibre"), e.get("Hortelã-pimenta"));
        receita("Molho de tomate com orégano e tomilho",
                "6 tomates maduros; 2 dentes de alho; 2 colheres de sopa de azeite; 1 colher de chá de orégano; 3 ramos de tomilho; sal a gosto",
                "Refogue o alho no azeite, junte os tomates picados, o sal e o tomilho e cozinhe por 20 minutos. Finalize com o orégano.",
                e.get("Orégano"), e.get("Tomilho"));
        receita("Massa com manteiga e sálvia",
                "250 g de massa; 4 colheres de sopa de manteiga; 10 folhas de sálvia; queijo ralado e sal a gosto",
                "Cozinhe a massa. Derreta a manteiga, doure as folhas de sálvia por 1 minuto e misture à massa escorrida. Sirva com queijo.",
                e.get("Sálvia"));
        receita("Arroz dourado com cúrcuma e cebolinha",
                "1 xícara de arroz; 1 colher de chá de cúrcuma em pó; 2 xícaras de água; 1 colher de sopa de óleo; cebolinha e sal a gosto",
                "Refogue o arroz com a cúrcuma no óleo, junte a água quente e o sal e cozinhe até secar. Finalize com a cebolinha picada.",
                e.get("Cúrcuma"), e.get("Cebolinha"));
        receita("Vinagrete verde de salsinha e coentro",
                "1 tomate; 1/2 cebola; 3 colheres de sopa de salsinha picada; 2 colheres de sopa de coentro picado; azeite, vinagre e sal a gosto",
                "Pique o tomate e a cebola, misture com as ervas, tempere com azeite, vinagre e sal e deixe descansar por 10 minutos.",
                e.get("Salsinha"), e.get("Coentro"));
        receita("Salada de folhas com pétalas de calêndula",
                "2 xícaras de folhas verdes; 1 punhado de pétalas de calêndula; azeite, limão e sal a gosto",
                "Tempere as folhas com azeite, limão e sal e espalhe as pétalas de calêndula por cima na hora de servir.",
                e.get("Calêndula"));
        receita("Biscoitinhos de lavanda e erva-doce",
                "2 xícaras de farinha; 100 g de manteiga; 1/2 xícara de açúcar; 1 colher de chá de flores de lavanda culinária; 1 colher de chá de erva-doce",
                "Misture todos os ingredientes até formar uma massa, modele os biscoitos e asse a 180 °C por cerca de 15 minutos.",
                e.get("Lavanda"), e.get("Erva-doce"));

        ritual("Sachê de lavanda para a hora de dormir",
                "Criar um momento simbólico de calma e desaceleração antes do sono.",
                "Encha um saquinho de tecido com flores secas de lavanda, amarre e deixe perto do travesseiro ou da cabeceira.",
                "Mantenha longe de crianças pequenas e de animais; evite se o aroma causar incômodo ou alergia.",
                e.get("Lavanda"));
        ritual("Banho de erva-cidreira e capim-limão",
                "Marcar um momento de pausa e acolhimento no fim do dia, de forma simbólica.",
                "Prepare uma infusão das duas ervas, deixe amornar, coe e use como último enxágue após o banho.",
                "Teste em uma pequena área da pele antes e evite em caso de alergia.",
                e.get("Erva-cidreira"), e.get("Capim-limão"));
        ritual("Vaso de arruda na entrada de casa",
                "Representar, em tradições populares, a proteção simbólica do lar.",
                "Cultive um vaso de arruda perto da porta de entrada, regando pouco e podando com luvas quando necessário.",
                "Planta tóxica: nunca ingerir nem fazer chás. O contato com a seiva ao sol pode queimar a pele; use luvas e mantenha longe de crianças e animais.",
                e.get("Arruda"));
        ritual("Defumação de sálvia",
                "Simbolizar a renovação do ambiente depois de uma mudança ou de um período difícil.",
                "Queime um pequeno maço seco de sálvia em recipiente à prova de fogo, com a janela aberta, e percorra os cômodos com cuidado.",
                "Mantenha longe de materiais inflamáveis e de crianças; apague completamente antes de sair do ambiente.",
                e.get("Sálvia"));
        ritual("Mesa da abundância com gengibre e cúrcuma",
                "Simbolizar disposição e prosperidade no início de um novo ciclo.",
                "Disponha um pedaço de gengibre e um de cúrcuma em um prato sobre a mesa e, depois do ritual, use-os para temperar uma refeição.",
                "Use apenas como simbolismo e lave bem os rizomas antes de cozinhá-los; a cúrcuma mancha tecidos.",
                e.get("Gengibre"), e.get("Cúrcuma"));
        ritual("Chá da intenção com hortelã-pimenta e boldo",
                "Reservar alguns minutos de silêncio para definir uma intenção para o dia.",
                "Prepare a infusão das duas ervas, segure a xícara em silêncio, pense na intenção e beba devagar.",
                "Informação educativa: gestantes, lactantes e pessoas com condições de saúde devem consultar um profissional antes de ingerir.",
                e.get("Hortelã-pimenta"), e.get("Boldo"));
        ritual("Bênção da cozinha com ervas frescas",
                "Agradecer pelos alimentos e valorizar o ato de cozinhar, de forma simbólica.",
                "Antes de cozinhar, reúna ramos de orégano, tomilho, salsinha, coentro e cebolinha em um copo com água e deixe na bancada durante o preparo.",
                "Troque a água diariamente e lave bem as ervas antes de consumir.",
                e.get("Orégano"), e.get("Tomilho"), e.get("Salsinha"), e.get("Coentro"), e.get("Cebolinha"));
        ritual("Oferenda de calêndula e erva-doce",
                "Expressar gratidão de forma simbólica por conquistas e por pessoas queridas.",
                "Disponha pétalas de calêndula e algumas sementes de erva-doce em um pires e deixe em um local especial por um dia, depois devolva à terra.",
                "Evite em caso de alergia a Asteráceas e mantenha longe de animais domésticos.",
                e.get("Calêndula"), e.get("Erva-doce"));
    }

    private Erva completa(String popular, String cientifico, CategoriaErva categoria, String origem, String descricao,
                          String parte, String cultivo, String caracteristicas, String advertencias,
                          TipoUso tipo, String usoDescricao, String preparo, String contraindicacoes) {
        Erva e = erva(popular, cientifico, categoria, origem, descricao);
        ficha(e, parte, cultivo, caracteristicas, advertencias);
        uso(e, tipo, usoDescricao, preparo, contraindicacoes);
        return e;
    }

    private Erva erva(String popular, String cientifico, CategoriaErva categoria, String origem, String descricao) {
        Erva e = new Erva();
        e.setNomePopular(popular);
        e.setNomeCientifico(cientifico);
        e.setCategoria(categoria);
        e.setOrigem(origem);
        e.setDescricao(descricao);
        return ervaRepository.save(e);
    }

    private void ficha(Erva erva, String parte, String cultivo, String caracteristicas, String advertencias) {
        FichaBotanica f = new FichaBotanica();
        f.setParteUtilizada(parte);
        f.setFormaCultivo(cultivo);
        f.setCaracteristicas(caracteristicas);
        f.setAdvertencias(advertencias);
        f.setErva(erva);
        fichaRepository.save(f);
    }

    private void uso(Erva erva, TipoUso tipo, String descricao, String preparo, String contraindicacoes) {
        Uso u = new Uso();
        u.setTipo(tipo);
        u.setDescricao(descricao);
        u.setModoPreparo(preparo);
        u.setContraindicacoes(contraindicacoes);
        u.setErva(erva);
        usoRepository.save(u);
    }

    private Receita receita(String nome, String ingredientes, String preparo, Erva... ervas) {
        Receita r = new Receita();
        r.setNome(nome);
        r.setIngredientes(ingredientes);
        r.setModoPreparo(preparo);
        for (Erva e : ervas) {
            r.getErvas().add(e);
        }
        return receitaRepository.save(r);
    }

    private void ritual(String nome, String finalidade, String instrucoes, String cuidados, Erva... ervas) {
        Ritual r = new Ritual();
        r.setNome(nome);
        r.setFinalidadeEspiritual(finalidade);
        r.setInstrucoes(instrucoes);
        r.setCuidados(cuidados);
        for (Erva e : ervas) {
            r.getErvas().add(e);
        }
        ritualRepository.save(r);
    }
}
