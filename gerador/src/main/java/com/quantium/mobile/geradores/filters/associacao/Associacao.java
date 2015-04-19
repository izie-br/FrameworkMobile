package com.quantium.mobile.geradores.filters.associacao;

import com.quantium.mobile.geradores.javabean.ModelSchema;

public abstract class Associacao {

    protected static final String ERRO_ARGUMENTOS_NULL_MSG =
            Associacao.class.getSimpleName() +
                    ":: Nenhum dos argumentos do contrutor pode ser null";

    private ModelSchema tabelaA;
    private ModelSchema tabelaB;

    public Associacao(ModelSchema tabelaA, ModelSchema tabelaB) {
        if (tabelaA == null || tabelaB == null)
            throw new RuntimeException(ERRO_ARGUMENTOS_NULL_MSG);
        this.tabelaA = tabelaA;
        this.tabelaB = tabelaB;
    }

    @Override
    public String toString() {
        return tabelaA.getName() + " " +
                getClass().getSimpleName() + " " +
                tabelaB.getName();
    }

    public ModelSchema getTabelaA() {
        return tabelaA;
    }

    public ModelSchema getTabelaB() {
        return tabelaB;
    }

    @Override
    public int hashCode() {
        return tabelaB.hashCode() +
                tabelaA.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Associacao other = (Associacao) obj;
        if (!tabelaB.equals(other.tabelaB))
            return false;
        if (!tabelaA.equals(other.tabelaA))
            return false;
        return true;
    }

}
