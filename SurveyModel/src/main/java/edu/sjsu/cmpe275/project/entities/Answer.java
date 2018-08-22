package edu.sjsu.cmpe275.project.entities;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;

@Entity
public class Answer {
	
	@Id
	@GeneratedValue
	@JsonView(View.Answer.class)
	private int id;
	
	@ManyToOne
	@JoinColumn(name="survey_id")
	@JsonView(View.Answer.class)
	private Survey survey;
	@JsonView(View.Answer.class)
	private String email;
	
	@OneToMany(cascade = {CascadeType.ALL},mappedBy="answer")
	@JsonView(View.Answer.class)
	private List<AnswerQuestion> aq;
	
	public Answer() {
		
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public Survey getSurvey() {
		return survey;
	}


	public void setSurvey(Survey survey) {
		this.survey = survey;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public List<AnswerQuestion> getAq() {
		return aq;
	}


	public void setAq(List<AnswerQuestion> aq) {
		this.aq = aq;
	}
	
	

}
