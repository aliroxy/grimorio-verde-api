package br.com.grimorioverde.security;

/**
 * Níveis de acesso de uma chave de API. A ordem importa:
 * um nível maior inclui as permissões dos menores.
 */
public enum NivelAcesso {

    /** Pode cadastrar, alterar e remover ervas na v2. */
    USUARIO,

    /** Tudo do USUARIO + listar e revogar chaves de API. */
    ADMIN
}
