package edu.sjsu.cmpe275.project.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;

@Entity
public class Account {
	
	@Id
	@GeneratedValue
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private int id;
	@NotEmpty
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private String email;
	@NotEmpty
	//@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	@JsonView({View.Account.class})
	private String password;
	
	private int code;
	private boolean status;
	
	@OneToMany(cascade = { CascadeType.ALL }, mappedBy="account")
	@JsonView({View.Account.class,View.Question.class})
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<Survey> surveys;
	
	public Account() {
		
	}


	public Account(String email, String password, int code, boolean status) {		
		this.email = email;
		this.password = password;
		this.code = code;
		this.status = status;
	}


	public Integer getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public List<Survey> getSurveys() {
		return surveys;
	}


	public void setSurveys(List<Survey> surveys) {
		this.surveys = surveys;
	}
	
	public void addSurvey(Survey survey) {
		this.surveys.add(survey);
	}


	public int getCode() {
		return code;
	}


	public void setCode(int code) {
		this.code = code;
	}


	public boolean isStatus() {
		return status;
	}


	public void setStatus(boolean status) {
		this.status = status;
	}

	
}
