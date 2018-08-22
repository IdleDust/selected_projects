package edu.sjsu.cmpe275.project.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Invitation {
	
	//only record closed invitation-only survey invitation link email
	@Id
	@GeneratedValue
	private int id;
	private String link;
	private String email;	
	
	@ManyToOne
	private Survey survey;

	public Invitation() {
		
	}

	public Invitation(String link, String email, Survey survey) {
		this.link = link;
		this.email = email;
		this.survey = survey;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Survey getSurvey() {
		return survey;
	}

	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	
	
}