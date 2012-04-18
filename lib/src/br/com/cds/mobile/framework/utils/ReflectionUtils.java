package br.com.cds.mobile.framework.utils;
//package br.com.cds.mobile.framework.utils;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//
//public class RelectionUtils {
//
//	public static class Property<T>{
//
//		private Object bean;
//		private Method getter;
//		private Method setter;
//		private Class<?> type;
//
//		public Property(Object bean,String name){
//			if(bean==null||name==null)
//				throw new RuntimeException(
//						"Objeto ou nome nao podem ser null.\n"+
//						"Objeto:: " + bean +
//						"\nNome::" + name
//				);
//			Class<?> klass = bean.getClass();
//			String capitalizedName =
//					"" + Character.toUpperCase(name.charAt(0)) +
//					name.substring(1);
//
//			Field field = null;
//			try {
//				field = klass.getDeclaredField(name);
//			} catch (SecurityException e) {}
//			catch (NoSuchFieldException e) {}
//
//
//			boolean noSuchGetter = false;
//			try {
//				getter = klass.getDeclaredMethod("get"+capitalizedName);
//			} catch (SecurityException e) {}
//			catch (NoSuchMethodException e) {
//				noSuchGetter = true;
//			}
//
//			if(field==null&&noSuchGetter)
//				throw new RuntimeException("Propriedade "+name+" da classe "+klass.getName()+" nao encontrada");
//			if(field==null&&getter==null)
//				throw new RuntimeException("Propriedade "+name+" da classe "+klass.getName()+" protegida contra acesso");
//			type =
//				(getter!=null) ?
//					getter.getReturnType():
//					field.getType();
//
//			try {
//				setter = klass.getDeclaredMethod("set"+capitalizedName, type);
//			} catch (SecurityException e) {}
//			catch (NoSuchMethodException e) {}
//
//			if(getter==null&&setter==null)
//				throw new RuntimeException(
//						"Propriedade "+name+" da classe "+klass.getName()+
//						" nao encontrada,mas possui campo compativel"
//				);
//		}
//
//		public boolean isReadOnly(){
//			return setter == null;
//		}
//
//		public boolean isWriteOnly(){
//			return getter == null;
//		}
//
//		@SuppressWarnings("unchecked")
//		public T get(){
//			try {
//				return (T)getter.invoke(bean);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		public void set(T value){
//			try {
//				setter.invoke(bean, value);
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//
//	}
//}
