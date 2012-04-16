package br.com.cds.mobile.geradores.javabean;

public class Propriedade {

	private String nome;
	private Class<?> type;
	private boolean get;
	private boolean set;

	
	public Propriedade(String nome, Class<?> type, boolean get, boolean set) {
		super();
		this.nome = nome;
		this.type = type;
		this.get = get;
		this.set = set;
	}

	public String getNome() {
		return nome;
	}


	public Class<?> getType() {
		return type;
	}


	public boolean isGet() {
		return get;
	}


	public boolean isSet() {
		return set;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (get ? 1231 : 1237);
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
		result = prime * result + (set ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Propriedade other = (Propriedade) obj;
		if (get != other.get)
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (set != other.set)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}



}
