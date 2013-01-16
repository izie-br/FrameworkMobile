package com.quantium.mobile.geradores.velocity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class VelocitySqlXmlFactory {

	private OutputStream out;
	private Template template;

	public VelocitySqlXmlFactory(VelocityEngine ve, OutputStream out) {
		this.out = out;
		this.template = ve.getTemplate ("sql.xml");
	}

	public void generateSqlXml(Map<String,InputStream> scripts) {
		Collection<DbVersion> versions = new ArrayList<DbVersion> ();
		for (String name : scripts.keySet ()) {
			DbVersion version = new DbVersion ();
			version.name = name;
			StringBuilder sb = new StringBuilder ();
			try {
				BufferedReader reader = new BufferedReader (
						new InputStreamReader (
								scripts.get (name),
								"UTF-8"
						)
				);
				char buffer [] = new char[255];
				int l = reader.read (buffer);
				while (l > 0) {
					String ref = new String(buffer, 0, l);
					sb.append (ref);
					l = reader.read (buffer);
				}
				reader.close ();
			} catch (Exception e) {
				throw new RuntimeException (e);
			}
			version.script = sb.toString ();
			versions.add (version);
		}
		VelocityContext ctx= new VelocityContext ();
		ctx.put ("versions", versions);
		try {
			BufferedWriter writer = new BufferedWriter (
					new OutputStreamWriter (this.out, "UTF-8")
			);
			template.merge (ctx, writer);
			writer.close ();
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}

	public static class DbVersion {
		private String name;
		private String script;
		public String getName() {
			return name;
		}
		public String getScript() {
			return script;
		}
	}

}
