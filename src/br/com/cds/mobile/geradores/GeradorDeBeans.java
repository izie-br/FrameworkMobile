package br.com.cds.mobile.geradores;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
import br.com.cds.mobile.geradores.sqlparser.SqlTabelaSchema;
import br.com.cds.mobile.geradores.tabelaschema.AssociacaoEntreTabelasWrapper;
import br.com.cds.mobile.geradores.tabelaschema.CamelCaseTabelaDecorator;
import br.com.cds.mobile.geradores.tabelaschema.PrefixoTabelaDecorator;
import br.com.cds.mobile.geradores.tabelaschema.TabelaSchema;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
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
		String colunaId = "id";
		String pacote = "br.com.cds.mobile.flora.eb";

		Map<String,TabelaSchema> tabelasMap = new HashMap<String, TabelaSchema>();

		// TODO separar este bloco
		{
			Collection<TabelaSchema> tabelasBanco =
					getTabelasDoSchema(new FileReader("script/schema.sql"));

			for(TabelaSchema tabela : tabelasBanco){
				tabela = new CamelCaseTabelaDecorator(
						new PrefixoTabelaDecorator("tb_",
						tabela
				));
				tabelasMap.put(tabela.getNome(), tabela);
			}
		
		}

		JCodeModel jcm = new JCodeModel();
		CodeModelBeanFactory jbf = new CodeModelBeanFactory(jcm);
		CodeModelDaoFactory daoFactory = new CodeModelDaoFactory(jcm);

		Map<String,AssociacaoEntreTabelasWrapper> tabelasAssociadas =
				new AssociacaoEntreTabelasWrapper.Mapeador().mapear(tabelasMap.values());

		Map<String,JDefinedClass> classesMap = new HashMap<String, JDefinedClass>();
		for(TabelaSchema tabela : tabelasAssociadas.values()){
			Map<String,Class<?>> propriedadesMap = new HashMap<String, Class<?>>();
			for(String coluna : tabela.getPropriedades().keySet()){
				propriedadesMap.put(
						coluna,
						tabela.getPropriedades().get(coluna).getType()
				);
			}
			JDefinedClass classeGerada = jbf.gerarPropriedades(
					jbf.gerarClasse(pacote+"."+tabela.getNome()),
					propriedadesMap
			);
			classesMap.put(tabela.getNome(), classeGerada);
		}

		// relacoes entre tabelas
		for(JDefinedClass classeGerada : classesMap.values()){
			AssociacaoEntreTabelasWrapper associacoes = tabelasAssociadas.get(classeGerada.name());
			for(TabelaSchema estrangeira : associacoes.getTabelasHasOne())
				jbf.gerarAssociacaoToOne(classeGerada, classesMap.get(estrangeira.getNome()), colunaId);
			for(TabelaSchema estrangeira : associacoes.getTabelasHasMany())
				jbf.gerarAssociacaoToMany(classeGerada, classesMap.get(estrangeira.getNome()), colunaId);
		}

		// gera metodos de acesso a banco
		for(JDefinedClass classeGerada : classesMap.values()){
			AssociacaoEntreTabelasWrapper schema = tabelasAssociadas.get(classeGerada.name());
			Map<String,JFieldVar> constantes = 
					daoFactory.gerarConstantesComNomesDasColunas(classeGerada, schema);
			String campoId = null;
			for(String coluna : schema.getPropriedades().keySet()){
				if(schema.getPropriedades().get(coluna).getNome().equals(colunaId))
					campoId = coluna;
			}
			// TODO
			daoFactory.gerarAcessoDB(classeGerada,schema.getWrappedTabelaSchema(),"ID","id");
		}

		//TODO gerar serialVersionUID

		jcm.build(new File("customGen"));
	}

	public static Collection<TabelaSchema> getTabelasDoSchema(Reader input) throws IOException {
		BufferedReader reader = new BufferedReader(input);
		Collection<TabelaSchema> tabelas = new ArrayList<TabelaSchema>();
		for(;;){
			String createTableStatement = reader.readLine();
			if(createTableStatement==null||createTableStatement.matches("^[\\s\\n]*$"))
				break;
			TabelaSchema tabela = new SqlTabelaSchema(createTableStatement);
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
