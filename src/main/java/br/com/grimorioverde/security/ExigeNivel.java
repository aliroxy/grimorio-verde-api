package br.com.grimorioverde.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um método de controller como protegido: a requisição precisa trazer
 * o cabeçalho X-API-Key com uma chave válida de nível igual ou superior.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExigeNivel {

    NivelAcesso value();
}
