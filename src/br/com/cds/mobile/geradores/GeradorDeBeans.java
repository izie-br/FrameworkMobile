package br.com.cds.mobile.geradores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

import br.com.cds.mobile.geradores.dao.CodeModelDaoFactory;
import br.com.cds.mobile.geradores.filters.CamelCaseFilter;
import br.com.cds.mobile.geradores.filters.PrefixoTabelaFilter;
import br.com.cds.mobile.geradores.filters.associacao.AssociacaoPorNomeFilter;
import br.com.cds.mobile.geradores.javabean.JavaBeanSchema;
import br.com.cds.mobile.geradores.javabean.Propriedade;
import br.com.cds.mobile.geradores.json.CodeModelJsonSerializacaoFactory;
import br.com.cds.mobile.geradores.sqlparser.SqlTabelaSchemaFactory;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

public class GeradorDeBeans {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// exemploDeUsoDoCodeModel();
		// exemploDeUsoJSqlParser();

		// TODO receber estas variaveis por comando de linha
		String pacote = "br.com.cds.mobile.flora.eb";
		String arquivo = "script/schema.sql";

		Collection<TabelaSchema> tabelasBanco =
				getTabelasDoSchema(new FileReader(arquivo));

		Collection<JavaBeanSchema> javaBeanSchemas = new ArrayList<JavaBeanSchema>();
		JavaBeanSchema.Factory factory = new JavaBeanSchema.Factory();
		factory.addFiltroFactory(new PrefixoTabelaFilter.Factory("tb_"));
		factory.addFiltroFactory(new AssociacaoPorNomeFilter.Factory("id_${TABELA}"));
		factory.addFiltroFactory(new CamelCaseFilter.Factory());
		for(TabelaSchema tabela : tabelasBanco)
			javaBeanSchemas.add(factory.javaBeanSchemaParaTabela(tabela));

		JCodeModel jcm = new JCodeModel();
		CodeModelBeanFactory jbf = new CodeModelBeanFactory(jcm);
		CodeModelDaoFactory daoFactory = new CodeModelDaoFactory(jcm);
		CodeModelJsonSerializacaoFactory jsonFactory = new CodeModelJsonSerializacaoFactory(jcm);

		Map<String, JDefinedClass> classesMap = new HashMap<String, JDefinedClass>();
		for(JavaBeanSchema javaBeanSchema : javaBeanSchemas){
			JDefinedClass classeGerada =  jbf.gerarClasse(
					pacote+"."+javaBeanSchema.getNome());
			jbf.gerarConstantes(classeGerada, javaBeanSchema);
			for(String coluna : javaBeanSchema.getColunas()){
				Propriedade p = javaBeanSchema.getPropriedade(coluna);
				if(p!=null)
					jbf.gerarPropriedade(classeGerada,p);
			}
			jbf.gerarMetodoClone(classeGerada, javaBeanSchema);
			jsonFactory.gerarMetodosDeSerializacaoJson(classeGerada, javaBeanSchema);
			classesMap.put(javaBeanSchema.getNome(), classeGerada);
		}

//		// relacoes entre tabelas
//		for(JDefinedClass classeGerada : classesMap.values()){
//			AssociacaoEntreTabelasWrapper associacoes = tabelasAssociadas.get(classeGerada.name());
//			for(TabelaSchema estrangeira : associacoes.getTabelasHasOne())
//				jbf.gerarAssociacaoToOne(classeGerada, classesMap.get(estrangeira.getNome()), colunaId);
//			for(TabelaSchema estrangeira : associacoes.getTabelasHasMany())
//				jbf.gerarAssociacaoToMany(classeGerada, classesMap.get(estrangeira.getNome()), colunaId);
//		}

//		for(JavaBeanSchema javaBeanSchema : javaBeanSchemas){
//			for(String fk : javaBeanSchema.getAssociacoesTemUm().keySet()){
//				for(JavaBeanSchema it : javaBeanSchemas)
//					if(it.getTabela().get)
//			}
//		}

		// gera metodos de acesso a banco
		for(JavaBeanSchema javaBeanSchema : javaBeanSchemas){
			daoFactory.gerarAcessoDB(classesMap.get(javaBeanSchema.getNome()),javaBeanSchema);
			for(JavaBeanSchema jb2 : javaBeanSchemas)
				daoFactory.gerarRelacoes(
						classesMap.get(javaBeanSchema.getNome()), javaBeanSchema,
						classesMap.get(jb2.getNome()), jb2
				);
		}

		//TODO gerar serialVersionUID

		jcm.build(new File("customGen"));
	}

	public static Collection<TabelaSchema> getTabelasDoSchema(Reader input) throws IOException {
		BufferedReader reader = new BufferedReader(input);
		Collection<TabelaSchema> tabelas = new ArrayList<TabelaSchema>();
		SqlTabelaSchemaFactory factory = new SqlTabelaSchemaFactory();
		for(;;){
			StringBuilder sb = new StringBuilder();
			int c;
			for(;;){
				c = reader.read();
				if(c<0)
					break;
				sb.append((char)c);
				if(c==';')
					break;
			}
			String createTableStatement = sb.toString();
			if(
					Pattern.compile("create\\s+view", Pattern.CASE_INSENSITIVE|Pattern.MULTILINE)
						.matcher(createTableStatement)
						.find()
			)
				continue;;
			if(createTableStatement==null||createTableStatement.matches("^[\\s\\n]*$"))
				break;
			TabelaSchema tabela = factory.gerarTabelaSchema(createTableStatement);
			tabelas.add(tabela);
		}
		return tabelas;
	}


	/***********************************
	 * Exemplos de uso das bibliotecas *
	 *     jCodeModel e jSqlParser     *
	 **********************************/


	/**
	 * exemplo de uso do codemodel
	 * @throws JClassAlreadyExistsException
	 * @throws IOException
	 */
	public static void exemploDeUsoDoCodeModel()
			throws JClassAlreadyExistsException, IOException {
		JCodeModel jcm = new JCodeModel();
		//classe
		JDefinedClass klass = jcm._class(JMod.PUBLIC,"br.com.cds.mobile.flora.eb.ClienteBean",ClassType.CLASS);
		JDocComment comment = klass.javadoc();
		comment.add("Classe gerada automaticamente");
		// public BigDecimal precoFromString(String precoString)
		JMethod metodo = klass.method(JMod.PUBLIC, BigDecimal.class, "precoFromString");
		comment = metodo.javadoc();
		comment.add("Comentario do metodo");
		JVar var = metodo.param(jcm._ref(String.class), "precoString");
		//escrevendo o corpo
		JBlock blocoMetodo = metodo.body();
		// if(precoString==null||precoString.equals(""))
		JConditional cond = blocoMetodo._if(var.eq(JExpr._null()).cor(var.invoke("trim").invoke("equals").arg("")));
		// precoString = new String("0")
		cond._then().assign(var, JExpr._new(jcm._ref(String.class)).arg("0"));
		// else
		// System.out.print("OK")
		cond._else().invoke(jcm.ref(System.class).staticRef("out"),"print").arg("OK");
		// return new BigDecimal(precoString)
		blocoMetodo._return(JExpr._new(jcm.ref(BigDecimal.class)).arg(var));
		jcm.build(new File("customGen"));
	}

	public static void exemploDeUsoJSqlParser(){
		try {
			CCJSqlParserManager manager = new CCJSqlParserManager();
			
			BufferedReader reader = new BufferedReader(new FileReader("script/schema.sql"));
			for(;;){
				String stringStm = reader.readLine();
				if(stringStm==null)
					break;
				Statement stm = manager.parse(new StringReader(stringStm));
				stm.accept(new StatementVisitor() {
					
					@Override
					public void visit(CreateTable ct) {
						System.out.print("create ");
						System.out.println(ct.getTable().getName());
						for(Object obj :ct.getColumnDefinitions())
							System.out.println( ((ColumnDefinition)obj).getColumnName()+"  "+ ((ColumnDefinition)obj).getColDataType().getDataType() );
						System.out.println("-------------");
					}
					
					@Override public void visit(Truncate arg0) {}
					@Override public void visit(Drop arg0) {}
					@Override public void visit(Replace arg0) {}
					@Override public void visit(Insert arg0) {}
					@Override public void visit(Update arg0) {}
					@Override public void visit(Delete arg0) {}
					@Override public void visit(Select arg0) {}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
