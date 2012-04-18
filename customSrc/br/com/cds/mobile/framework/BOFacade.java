package br.com.cds.mobile.framework;

import java.lang.ref.SoftReference;

public class BOFacade {

	private static SoftReference<BOFacade> instance;

	public static BOFacade getInstance() {
		BOFacade bofacade = null;
		if (instance != null)
			bofacade = instance.get();
		if(bofacade==null){
			bofacade = new BOFacade();
			instance = new SoftReference<BOFacade>(bofacade);
		}
		return bofacade;
	}


	public BOFacade() {
	}

}
