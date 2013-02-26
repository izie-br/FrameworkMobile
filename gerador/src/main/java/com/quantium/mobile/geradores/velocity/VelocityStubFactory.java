package com.quantium.mobile.geradores.velocity;

import java.io.File;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

import com.quantium.mobile.framework.utils.CamelCaseUtils;
import com.quantium.mobile.framework.utils.StringUtil;
import com.quantium.mobile.geradores.javabean.JavaBeanSchema;
import com.quantium.mobile.geradores.util.Constants;
import com.quantium.mobile.geradores.util.LoggerUtil;

public class VelocityStubFactory {

	public static enum StubType {
		INTERFACE, IMPLEMENTATION, DAO;

		public String getTemplateName() {
			switch (this) {
			case INTERFACE:
			case IMPLEMENTATION:
				return "VoStub.java";
			case DAO:
				return "DaoStub.java";
			default:
				throw new RuntimeException();
			}
		}

		public String getNameFor(JavaBeanSchema schema) {
			String nameUpper = CamelCaseUtils.toUpperCamelCase(schema.getNome());
			switch (this) {
			case INTERFACE:
				return nameUpper;
			case IMPLEMENTATION:
				return nameUpper + "Impl";
			case DAO:
				//TODO
			default:
				throw new RuntimeException();
			}
		}

	}

	private StubType type;
	private String basePackage;
	private String genPackage;
	private String voPackage;
	private File targetDirectory;
	private Template template;

	public VelocityStubFactory(
			VelocityEngine ve, StubType type, String basePackage,
			String genPackage, String voPackage, File targetDirectory)
	{
		this.type = type;
		this.basePackage = basePackage;
		this.genPackage = genPackage;
		this.voPackage = voPackage;
		this.targetDirectory = targetDirectory;
		this.template = ve.getTemplate(type.getTemplateName());
	}

	public boolean isStubFound (JavaBeanSchema schema) {
		String module = schema.getModule();
		File packageDir = targetDirectory;
		if (!module.equals(Constants.DEFAULT_MODULE_NAME)) {
			packageDir = new File(packageDir, module);
		}
		if (this.voPackage != null) {
			packageDir = new File(packageDir, this.voPackage);
		}
		if (!packageDir.exists()) {
			packageDir.mkdir();
		}
		String classFileName = this.type.getNameFor(schema) + ".java";
		try {
			File classFile = new File(packageDir, classFileName);
			boolean stubExists = classFile.exists();
			if (!stubExists) {
				LoggerUtil.getLog().info(String.format(
						"Arquivo stub %s nao encontrado.",
						classFile.getPath()));
			}
			return stubExists;
		} catch (Exception e) {
			LoggerUtil.getLog().error(StringUtil.getStackTrace(e));
		}
		return false;
	}

}
