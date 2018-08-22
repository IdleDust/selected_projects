package edu.sjsu.cmpe275.project.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;

@Entity
public class Survey {
	
	public enum SURVEY_TYPES{
		GENERAL,
		CLOSED_INVITATION,
		OPEN_UNIQUE,
		ANONYMOUS
	}
	
	@Id
	@GeneratedValue
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private int id;
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private SURVEY_TYPES surveyType;
	
	@ManyToOne
	@JoinColumn(name="account_id")
	@JsonView({View.Survey.class,View.Question.class})
	private Account account;
	
	@JsonFormat(
	      shape = JsonFormat.Shape.STRING,
	      pattern = "yyyy-MM-dd hh:mm")
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private Date startTime;
	
	@JsonFormat(
	      shape = JsonFormat.Shape.STRING,
	      pattern = "yyyy-MM-dd hh:mm")
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private Date endTime;
	
	@JsonFormat(
		  shape = JsonFormat.Shape.STRING,
		  pattern = "yyyy-MM-dd hh:mm")
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private Date updateTime;
	
	@OneToMany(cascade = { CascadeType.ALL },mappedBy="survey")
	@JsonProperty("questions")
	@JsonView({View.Survey.class})
	private List<Question> questions;
	
	@JsonView({View.Survey.class})
	private String link;
	
	@JsonView(View.Report.class)
	@Transient
	private Long participantNum;
	@JsonView(View.Report.class)
	@Transient
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String participationRate;
	
	public Survey() {
		
	}

	public Survey(SURVEY_TYPES surveyType, Date startTime, Date endTime) {
		this.surveyType = surveyType;
		this.startTime = startTime;
		this.endTime = endTime;
		this.updateTime = null;
		this.link = "";
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public SURVEY_TYPES getSurveyType() {
		return surveyType;
	}

	public void setSurveyType(SURVEY_TYPES surveyType) {
		this.surveyType = surveyType;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Long getParticipantNum() {
		return participantNum;
	}

	public void setParticipantNum(Long participantNum) {
		this.participantNum = participantNum;
	}

	public String getParticipationRate() {
		return participationRate;
	}

	public void setParticipationRate(String participationRate) {
		this.participationRate = participationRate;
	}
	
	public void addQuestion(Question q) {
		this.questions.add(q);
	}

}
