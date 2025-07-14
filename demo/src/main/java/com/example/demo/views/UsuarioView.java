package com.example.demo.views;

public class UsuarioView {

	private Long id;
	private String email;
	private String alias;
	private boolean esAlumno;
	private boolean registroCompleto;
	private boolean activo;

	public UsuarioView() {}

	public UsuarioView(Long id, String email, String alias, boolean esAlumno, boolean registroCompleto, boolean activo) {
		this.id = id;
		this.email = email;
		this.alias = alias;
		this.esAlumno = esAlumno;
		this.registroCompleto = registroCompleto;
		this.activo = activo;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean isEsAlumno() {
		return esAlumno;
	}

	public void setEsAlumno(boolean esAlumno) {
		this.esAlumno = esAlumno;
	}

	public boolean isRegistroCompleto() {
		return registroCompleto;
	}

	public void setRegistroCompleto(boolean registroCompleto) {
		this.registroCompleto = registroCompleto;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	@Override
	public String toString() {
		return "UsuarioView{" +
				"id=" + id +
				", email='" + email + '\'' +
				", alias='" + alias + '\'' +
				", esAlumno=" + esAlumno +
				", registroCompleto=" + registroCompleto +
				", activo=" + activo +
				'}';
	}
}
