/* Grimório Verde — página simples que consome a API (v2 para a lista, v1 para os detalhes). */
(function () {
    'use strict';

    var API = '/api';
    var TAMANHO_PAGINA = 9;

    var NOME_CATEGORIA = {
        MEDICINAL: 'Medicinal', CULINARIA: 'Culinária', ESPIRITUAL: 'Espiritual',
        AROMATICA: 'Aromática', ORNAMENTAL: 'Ornamental'
    };
    var NOME_TIPO_USO = { MEDICINAL: 'Medicinal', CULINARIO: 'Culinário', ESPIRITUAL: 'Espiritual' };

    var estado = { pagina: 0, nome: '', categoria: '', totalPaginas: 0 };

    function $(seletor) { return document.querySelector(seletor); }

    // Escapa texto vindo da API antes de colocá-lo no HTML.
    function esc(valor) {
        return String(valor == null ? '' : valor).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }

    function buscarJson(url) {
        return fetch(url, { headers: { Accept: 'application/json' } }).then(function (resposta) {
            if (resposta.ok) { return resposta.json(); }
            return resposta.json().catch(function () { return {}; }).then(function (erro) {
                var e = new Error(erro.mensagem || ('Erro ' + resposta.status));
                e.status = resposta.status;
                throw e;
            });
        });
    }

    // Respostas paginadas HAL guardam a lista em _embedded.<nome>; pegamos a primeira lista que houver.
    function itensDaPagina(pagina) {
        var embutido = pagina && pagina._embedded;
        if (!embutido) { return []; }
        var chave = Object.keys(embutido)[0];
        return chave ? embutido[chave] : [];
    }

    /* ---------- lista de ervas ---------- */

    function carregarErvas() {
        var lista = $('#lista');
        lista.innerHTML = '<p class="mensagem">Carregando…</p>';

        var params = new URLSearchParams({ page: estado.pagina, size: TAMANHO_PAGINA, sort: 'nomePopular,asc' });
        if (estado.nome) { params.set('nome', estado.nome); }
        if (estado.categoria) { params.set('categoria', estado.categoria); }

        buscarJson(API + '/v2/ervas?' + params.toString())
            .then(function (dados) {
                var ervas = itensDaPagina(dados);
                var info = dados.page || { totalElements: ervas.length, totalPages: 1, number: 0 };
                estado.totalPaginas = info.totalPages;
                mostrarErvas(ervas, info);
            })
            .catch(function (erro) {
                lista.innerHTML = '<p class="mensagem mensagem--erro">Não foi possível carregar as ervas: '
                    + esc(erro.message) + '</p>';
                $('#resumo').textContent = '';
                $('#paginacao').hidden = true;
            });
    }

    function mostrarErvas(ervas, info) {
        var lista = $('#lista');
        $('#resumo').textContent = info.totalElements === 1
            ? '1 erva encontrada.' : info.totalElements + ' ervas encontradas.';

        if (ervas.length === 0) {
            lista.innerHTML = '<p class="mensagem">Nenhuma erva encontrada com esses filtros.</p>';
        } else {
            lista.innerHTML = ervas.map(function (e) {
                var usos = e.totalUsos === 1 ? '1 uso' : e.totalUsos + ' usos';
                return '<article class="cartao">'
                    + '<span class="etiqueta">' + esc(NOME_CATEGORIA[e.categoria] || e.categoria) + '</span>'
                    + '<h3>' + esc(e.nomePopular) + '</h3>'
                    + '<p class="cartao__cientifico">' + esc(e.nomeCientifico) + '</p>'
                    + '<p class="cartao__descricao">' + esc(e.descricao) + '</p>'
                    + '<div class="cartao__rodape">'
                    + '<span class="cartao__info">' + usos + (e.possuiFichaBotanica ? ' · ficha botânica' : '') + '</span>'
                    + '<button type="button" class="botao" data-id="' + esc(e.id) + '">Ver detalhes</button>'
                    + '</div></article>';
            }).join('');
        }

        var nav = $('#paginacao');
        nav.hidden = info.totalPages <= 1;
        $('#paginaAtual').textContent = 'Página ' + (info.number + 1) + ' de ' + info.totalPages;
        $('#anterior').disabled = info.number <= 0;
        $('#proxima').disabled = info.number >= info.totalPages - 1;
    }

    /* ---------- janela de detalhes ---------- */

    function abrirDetalhes(id) {
        var janela = $('#detalhes');
        $('#detalhesTitulo').textContent = 'Carregando…';
        $('#detalhesCientifico').textContent = '';
        $('#detalhesCorpo').innerHTML = '';
        if (!janela.open) { janela.showModal(); }

        var consultas = [
            buscarJson(API + '/v1/ervas/' + id),
            buscarJson(API + '/v1/fichas-botanicas/erva/' + id),
            buscarJson(API + '/v1/usos/erva/' + id + '?size=50'),
            buscarJson(API + '/v1/receitas/erva/' + id + '?size=50'),
            buscarJson(API + '/v1/rituais/erva/' + id + '?size=50')
        ].map(function (p) {
            return p.then(function (v) { return { ok: true, valor: v }; },
                          function (e) { return { ok: false, erro: e }; });
        });

        Promise.all(consultas).then(function (r) {
            if (!r[0].ok) {
                $('#detalhesTitulo').textContent = 'Erro';
                $('#detalhesCorpo').innerHTML = '<p class="vazio">' + esc(r[0].erro.message) + '</p>';
                return;
            }
            var erva = r[0].valor;
            var ficha = r[1].ok ? r[1].valor : null; // 404 = erva sem ficha botânica
            var usos = r[2].ok ? itensDaPagina(r[2].valor) : [];
            var receitas = r[3].ok ? itensDaPagina(r[3].valor) : [];
            var rituais = r[4].ok ? itensDaPagina(r[4].valor) : [];

            $('#detalhesTitulo').textContent = erva.nomePopular;
            $('#detalhesCientifico').textContent = erva.nomeCientifico;
            $('#detalhesCorpo').innerHTML = montarDetalhes(erva, ficha, usos, receitas, rituais);
        });
    }

    function linha(rotulo, valor) {
        return valor ? '<p><span class="rotulo">' + esc(rotulo) + ':</span> ' + esc(valor) + '</p>' : '';
    }

    function secao(titulo, itens, desenhar, vazio) {
        return '<h3>' + esc(titulo) + '</h3>'
            + (itens.length ? itens.map(desenhar).join('') : '<p class="vazio">' + esc(vazio) + '</p>');
    }

    function montarDetalhes(erva, ficha, usos, receitas, rituais) {
        var html = '<h3>Sobre</h3><div class="item">'
            + '<span class="etiqueta">' + esc(NOME_CATEGORIA[erva.categoria] || erva.categoria) + '</span>'
            + '<p>' + esc(erva.descricao) + '</p>'
            + linha('Origem', erva.origem) + '</div>';

        html += '<h3>Informações botânicas</h3>';
        if (ficha) {
            html += '<div class="item">'
                + linha('Parte utilizada', ficha.parteUtilizada)
                + linha('Cultivo', ficha.formaCultivo)
                + linha('Características', ficha.caracteristicas)
                + (ficha.advertencias ? '<div class="alerta">⚠️ ' + esc(ficha.advertencias) + '</div>' : '')
                + '</div>';
        } else {
            html += '<p class="vazio">Esta erva ainda não tem ficha botânica.</p>';
        }

        html += secao('Formas de uso', usos, function (u) {
            return '<div class="item"><span class="etiqueta">' + esc(NOME_TIPO_USO[u.tipo] || u.tipo) + '</span>'
                + '<p>' + esc(u.descricao) + '</p>'
                + linha('Preparo', u.modoPreparo)
                + (u.contraindicacoes ? '<div class="alerta">⚠️ ' + esc(u.contraindicacoes) + '</div>' : '')
                + (u.aviso ? '<div class="alerta">' + esc(u.aviso) + '</div>' : '')
                + '</div>';
        }, 'Nenhum uso cadastrado.');

        html += secao('Receitas', receitas, function (r) {
            return '<div class="item"><strong>' + esc(r.nome) + '</strong>'
                + linha('Ingredientes', r.ingredientes) + linha('Modo de preparo', r.modoPreparo) + '</div>';
        }, 'Nenhuma receita cadastrada.');

        html += secao('Rituais', rituais, function (r) {
            return '<div class="item"><strong>' + esc(r.nome) + '</strong>'
                + linha('Finalidade', r.finalidadeEspiritual) + linha('Como fazer', r.instrucoes)
                + (r.cuidados ? '<div class="alerta">⚠️ ' + esc(r.cuidados) + '</div>' : '') + '</div>';
        }, 'Nenhum ritual cadastrado.');

        return html;
    }

    /* ---------- eventos ---------- */

    var temporizador;
    $('#busca').addEventListener('input', function (evento) {
        clearTimeout(temporizador);
        temporizador = setTimeout(function () {
            estado.nome = evento.target.value.trim();
            estado.pagina = 0;
            carregarErvas();
        }, 350);
    });

    $('#categoria').addEventListener('change', function (evento) {
        estado.categoria = evento.target.value;
        estado.pagina = 0;
        carregarErvas();
    });

    $('#filtros').addEventListener('submit', function (evento) { evento.preventDefault(); });

    $('#limpar').addEventListener('click', function () {
        $('#busca').value = '';
        $('#categoria').value = '';
        estado.nome = '';
        estado.categoria = '';
        estado.pagina = 0;
        carregarErvas();
    });

    $('#anterior').addEventListener('click', function () {
        if (estado.pagina > 0) { estado.pagina--; carregarErvas(); window.scrollTo({ top: 0, behavior: 'smooth' }); }
    });

    $('#proxima').addEventListener('click', function () {
        if (estado.pagina < estado.totalPaginas - 1) { estado.pagina++; carregarErvas(); window.scrollTo({ top: 0, behavior: 'smooth' }); }
    });

    $('#lista').addEventListener('click', function (evento) {
        var botao = evento.target.closest('button[data-id]');
        if (botao) { abrirDetalhes(botao.getAttribute('data-id')); }
    });

    $('#fechar').addEventListener('click', function () { $('#detalhes').close(); });
    $('#detalhes').addEventListener('click', function (evento) {
        if (evento.target === evento.currentTarget) { evento.currentTarget.close(); } // clique no fundo escuro
    });

    carregarErvas();
})();
