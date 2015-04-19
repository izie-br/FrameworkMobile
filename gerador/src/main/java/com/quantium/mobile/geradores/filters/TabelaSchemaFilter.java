package com.quantium.mobile.geradores.filters;

import com.quantium.mobile.framework.query.Table;
import com.quantium.mobile.geradores.filters.associacao.Associacao;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.javabean.Property;

import java.util.Collection;

/**
 * Padrao GOF Chain of Responsibility (apelidado de Filter)
 *
 * @author Igor Soares
 */
public abstract class TabelaSchemaFilter {

    private TabelaSchemaFilter proximo = null;

    /**
     * retorna o nome da tabela
     *
     * @return
     */
    public String getName() {
        return proximo.getName();
    }

    public String getModule() {
        return proximo.getModule();
    }

    public boolean isNonEntityTable() {
        return proximo.isNonEntityTable();
    }

    protected ModelSchema getModelSchema() {
        return proximo.getModelSchema();
    }

    public Property getPropriedade(String coluna) {
        return proximo.getPropriedade(coluna);
    }

    public Property getPrimaryKey() {
        return proximo.getPrimaryKey();
    }

    public Collection<Associacao> getAssociacoes() {
        return proximo.getAssociacoes();
    }

    public Table getTable() {
        return proximo.getTable();
    }

    public final void proximoFiltro(TabelaSchemaFilter filtro) {
        proximo = filtro;
    }

}
