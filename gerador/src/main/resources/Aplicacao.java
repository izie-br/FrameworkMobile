
import br.com.cds.mobile.db.DB;
import br.com.cds.mobile.framework.BaseApplication;

public class Aplicacao extends BaseApplication{

	@Override
	public int dbVersion() {
		return DB.DB_VERSAO;
	}

	@Override
	public String userName() {
		// TODO Auto-generated method stub
		return "";
	}

}