package com.quantium.mobile.geradores.filters.associacao;

import com.quantium.mobile.framework.validation.Constraint;
import com.quantium.mobile.geradores.filters.TabelaSchemaFilter;
import com.quantium.mobile.geradores.filters.TabelaSchemaFilterFactory;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.javabean.ModelSchema;
import com.quantium.mobile.geradores.javabean.Property;
import com.quantium.mobile.geradores.util.LoggerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Atencao! Filtro complexo!
 * <p/>
 * <p>Ha uma ligacao bidirecional entre este filtro e um
 * {@link AssociacoesResolver}.</p>
 * <p>O {@link AssociacoesResolver} deve contr uma referencia a todos os
 * {@link AssociacaoPorNomeFilter}, que servem de fonte de dados.</p>
 * <p/>
 * <p>Todos os filtros devem ser inicializados antes do uso (importante).
 * Na primeira chamada, o resolver ira iterar por todos os
 * {@link AssociacaoPorNomeFilter}, usando-os como fonte de dados.</p>
 * <p>Importante que todos {@link AssociacaoPorNomeFilter} ja estejam
 * inicializados antes de ser usado nos {@link JavaBeanSchema}</p>
 * <p>A inicializacao do {@link AssociacoesResolver} consiste em
 * iterar por todos os {@link AssociacaoPorNomeFilter} e em cada um, buscar
 * se ha um padrao de nome, da coluna
 * (nomeDaTabelaA)_(nomeDeColunaDaTabelaA).</p>
 * <ul>
 * <li>Criacao da {@link Factory}</li>
 * <li>A Factory criada inicializa 1 {@link AssociacoesResolver}, que e
 * ligada a todas as instancias {@link AssociacaoPorNomeFilter}</li>
 * <li>Para cada chamada a getFilterInstance, uma instancia de
 * {@link AssociacaoPorNomeFilter} e criada, em geral,
 * uma para cada JavaBeanSchema</li>
 * <li>Ao ser chamado pela primeira vez pelo {@link JavaBeanSchema}, o Filtro
 * chama a inicializacao do resolver</li>
 * <li>Chamadas subsequentes usam dados ja inicializados no
 * {@link AssociacoesResolver}</li>
 * </ul>
 *
 * @author Igor soares
 */
public class AssociacaoPorNomeFilter extends TabelaSchemaFilter {

    private static final String COLUMN_TABLE_NAME_REGEX = "\\w[\\w_\\d]*";
    private static final String SEPARATOR = "[,|]";
    private static final String MULTIPLE_COLUMN_TABLE_NAME_REGEX =
            COLUMN_TABLE_NAME_REGEX +
                    "(" + SEPARATOR + COLUMN_TABLE_NAME_REGEX + ")*";
    private static final String TABLE_PLACEHOLDER_WITH_FIXED_NAMES =
            "\\{TABLE(=(" + MULTIPLE_COLUMN_TABLE_NAME_REGEX + "))?\\}";
    private static final String COLUMN_PLACEHOLDER_REGEX_WITH_FIXED_NAMES =
            "\\{COLUMN(=(" + MULTIPLE_COLUMN_TABLE_NAME_REGEX + "))?\\}";

    private static final int COLUMN_INDEX_IN_FORMAT_STRING = 2;
    private static final int TABLE_INDEX_IN_FORMAT_STRING = 1;
    private static final int PLACEHOLDER_FIXED_NAMES_GROUP = 2;


    private static final String ERRO_PADRAO_CHAVE_ESTRANGEIRA_SEM_PLACEHOLDER_MSG_FORMAT =
            "O padrao de chave estrangeira deve conter uma ocorrencia de %s";

    private AssociacoesResolver resolver;

    @Override
    public boolean isNonEntityTable() {
        for (Associacao a : resolver.getAssociacoes()) {
            if (
                    a instanceof AssociacaoManyToMany &&
                            ((AssociacaoManyToMany) a).getTabelaJuncao()
                                    .getName().equals(
                                    super.getTable().getName()
                            )
                    ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo criado para nao ocorrer recursao infinita em
     * no metodo "mapear"do resolver
     *
     * @param coluna
     * @return
     */
    private String getPropriedadeNome(String coluna) {
        return super.getPropriedade(coluna).getNome();
    }

    @Override
    public Collection<Associacao> getAssociacoes() {
        Collection<Associacao> associacoesTemUm = new HashSet<Associacao>();
        associacoesTemUm.addAll(super.getAssociacoes());
        associacoesTemUm.addAll(resolver.getAssociacoes(getModelSchema().getName()));
        return associacoesTemUm;
    }

    private static class AssociacoesResolver {

        private String pattern;
        private Collection<String> onlyTables;
        private Collection<String> onlyColumns;
        private Collection<Associacao> associacoes;
        private Collection<String> mapeadas;
        private Collection<AssociacaoPorNomeFilter> filtros =
                new ArrayList<AssociacaoPorNomeFilter>();

        private Collection<Associacao> getAssociacoes() {
            validar();
            return associacoes;
        }

        private Collection<Associacao> getAssociacoes(String tabela) {
            Collection<Associacao> associacoesTemUm = new ArrayList<Associacao>();
            for (Associacao associacao : getAssociacoes()) {
                if (associacao.getTabelaA().getName().equals(tabela) ||
                        associacao.getTabelaB().getName().equals(tabela)) {
                    associacoesTemUm.add(associacao);
                }
            }
            return associacoesTemUm;
        }

        public void validar() {
            if (associacoes == null || mapeadas == null)
                mapear();
            for (AssociacaoPorNomeFilter filtro : filtros)
                if (!mapeadas.contains(filtro.getName()))
                    mapear();
        }

        private void mapear() {
            associacoes = new ArrayList<Associacao>();
            mapeadas = new ArrayList<String>();
            for (AssociacaoPorNomeFilter filtroTabelaA : filtros) {
                for (AssociacaoPorNomeFilter filtroTabelaB : filtros) {
                    if (onlyTables != null && !onlyTables.contains(filtroTabelaB.getName()))
                        continue;
                    for (Property propModelA : filtroTabelaA.getModelSchema().getProperties()) {
                        String colunaA = propModelA.getNome();
                        if (onlyColumns != null && !onlyColumns.contains(colunaA))
                            continue;
                        Object args[] = new String[2];
                        args[TABLE_INDEX_IN_FORMAT_STRING - 1] = filtroTabelaA.getName();
                        args[COLUMN_INDEX_IN_FORMAT_STRING - 1] = filtroTabelaA.getPropriedadeNome(colunaA);
                        // exemplo: colunaToA = "id_outratbela"
                        String colunaToA = String.format(pattern, args);
                        for (Property propModelB : filtroTabelaB.getModelSchema().getProperties()) {
                            String coluna = propModelB.getNome();
                            if (filtroTabelaB.getPropriedadeNome(coluna).equals(colunaToA)) {
                                Property prop = filtroTabelaA.getPropriedade(colunaA);
                                Constraint constraints[] = prop.getConstraints();
                                boolean nullable = true;
                                for (Constraint constraint : constraints) {
                                    if (constraint instanceof Constraint.NotNull) {
                                        nullable = false;
                                        break;
                                    }
                                }
                                try {
                                    Associacao associacao = new AssociacaoOneToMany(
                                            filtroTabelaA.getModelSchema(),
                                            filtroTabelaB.getModelSchema(),
                                            colunaToA,
                                            nullable,
                                            colunaA
                                    );
                                    associacoes.add(associacao);
                                } catch (RuntimeException e) {
                                    LoggerUtil.getLog().error("Erro na tabela " + filtroTabelaA.getName());
                                    LoggerUtil.getLog().error("Relacao com " + filtroTabelaB.getName() + " " + colunaToA);
                                    LoggerUtil.getLog().error(colunaToA);
                                    throw e;
                                }
                                break;
                            } // endif(filtroTabelaB.getPropriedadeNome(coluna.getNome()))
                        } // endfor(TabelaSchema.Coluna coluna : filtroTabelaB.getTabela().getColunas())
                    } // endfor(TabelaSchema.Coluna colunaA : filtroTabelaA.getTabela().getColunas())
                } // endfor(AssociacaoPorNomeFilter filtroTabelaB : filtros)
                mapeadas.add(filtroTabelaA.getName());
            } // endfor(AssociacaoPorNomeFilter filtroTabelaA :filtros)
            mapManyToManyRelationships();
        }

        private void mapManyToManyRelationships() {
            ArrayList<Associacao> associationsCopy = new ArrayList<Associacao>(associacoes);
            for (Associacao associationA : associationsCopy) {
                ModelSchema table = associationA.getTabelaB();
                if (
                        !(associationA instanceof AssociacaoOneToMany) ||
                                table.getProperties().size() > 3
                        ) {
                    continue;
                }
                AssociacaoPorNomeFilter filter = null;
                for (AssociacaoPorNomeFilter f : filtros) {
                    if (f.getModelSchema().equals(table)) {
                        filter = f;
                        break;
                    }
                }
                for (Associacao associationB : associationsCopy) {
                    if (
                            !associationB.getTabelaB().equals(table) ||
                                    !(associationB instanceof AssociacaoOneToMany)
                            ) {
                        continue;
                    }
                    boolean isManyToMany = true;
                    for (Property coluna : table.getProperties()) {
                        if (!(
                                filter.getPropriedadeNome(coluna.getNome()).equals(
                                        ((AssociacaoOneToMany) associationA).getKeyToA()
                                ) ||
                                        filter.getPropriedadeNome(coluna.getNome()).equals(
                                                ((AssociacaoOneToMany) associationB).getKeyToA()
                                        ) || (
                                        table.getPrimaryKey().equals(coluna)
                                )
                        )) {
                            isManyToMany = false;
                        }
                    }
                    if (isManyToMany) {
                        associacoes.remove(associationA);
                        associacoes.remove(associationB);
                        AssociacaoManyToMany manyToMany =
                                new AssociacaoManyToMany(
                                        associationA.getTabelaA(),
                                        associationB.getTabelaA(),
                                        ((AssociacaoOneToMany) associationA).getKeyToA(),
                                        ((AssociacaoOneToMany) associationB).getKeyToA(),
                                        ((AssociacaoOneToMany) associationA).getReferenciaA(),
                                        ((AssociacaoOneToMany) associationB).getReferenciaA(),
                                        table,
                                        ""
                                );
                        if (!associacoes.contains(manyToMany))
                            associacoes.add(manyToMany);
                    }
                } // for(Associacao associationB : associationsCopy)
            } // for(Associacao associationA : associationsCopy)
        }

    }


    public static class Factory implements TabelaSchemaFilterFactory {

        private AssociacoesResolver resolver;

        public Factory(String pattern) {
            Collection<String> onlyTables = extractFixedNames(
                    pattern,
                    TABLE_PLACEHOLDER_WITH_FIXED_NAMES
            );
            Pattern pat = Pattern.compile(TABLE_PLACEHOLDER_WITH_FIXED_NAMES);
            Matcher mobj = pat.matcher(pattern);
            mobj.find();
            pattern =
                    pattern.substring(0, mobj.start()) +
                            "%" + TABLE_INDEX_IN_FORMAT_STRING + "$s" +
                            pattern.substring(mobj.end());

            Collection<String> onlyCollumns = extractFixedNames(
                    pattern,
                    COLUMN_PLACEHOLDER_REGEX_WITH_FIXED_NAMES
            );
            pat = Pattern.compile(COLUMN_PLACEHOLDER_REGEX_WITH_FIXED_NAMES);
            mobj = pat.matcher(pattern);
            mobj.find();
            pattern =
                    pattern.substring(0, mobj.start()) +
                            "%" + COLUMN_INDEX_IN_FORMAT_STRING + "$s" +
                            pattern.substring(mobj.end());

            resolver = new AssociacoesResolver();
            resolver.pattern = pattern;
            resolver.onlyColumns = onlyCollumns;
            resolver.onlyTables = onlyTables;
        }

        protected Collection<String> extractFixedNames(String pattern, String placeholder) {
            Collection<String> fixedNames = null;
            Pattern tableRegex = Pattern.compile(placeholder);
            Matcher mobj = tableRegex.matcher(pattern);
            if (!mobj.find())
                throw new RuntimeException(String.format(
                        ERRO_PADRAO_CHAVE_ESTRANGEIRA_SEM_PLACEHOLDER_MSG_FORMAT,
                        placeholder
                ));
            if (mobj.group(PLACEHOLDER_FIXED_NAMES_GROUP) != null) {
                fixedNames = new HashSet<String>();
                String[] names = mobj.group(PLACEHOLDER_FIXED_NAMES_GROUP).split(SEPARATOR);
                fixedNames.addAll(Arrays.asList(names));
            }
            return fixedNames;
        }

        @Override
        public TabelaSchemaFilter getFilterInstance() {
            AssociacaoPorNomeFilter filtro = new AssociacaoPorNomeFilter();
            // associando os filtros ao resolver bidirecionalmente
            resolver.filtros.add(filtro);
            filtro.resolver = this.resolver;
            return filtro;
        }

    }

}
