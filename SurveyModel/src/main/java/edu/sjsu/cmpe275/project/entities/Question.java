package edu.sjsu.cmpe275.project.entities;


import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;

@Entity
public class Question {
	
	public enum QUESTION_TYPES{
//		MULTIPLE_CHOICE_TEXT,
//		MULTIPLE_CHOICE_IMAGE,
//		MULTIPLE_CHOICE_CHECKBOX,
//		SINGLE_CHOICE_TEXT,
//		SINGLE_CHOICE_IMAGE,
//		SINGLE_CHOICE_DROPDOWN,
//		SINGLE_CHOICE_RADIO,
//		SINGLE_CHOICE_CHECKBOX,
//		YES_NO,
//		SHORT_ANSWER,
//		DATE_TIME,
//		STAR_RATING
		MULTIPLE_CHOICE_IMAGE_CHECKBOX,
		MULTIPLE_CHOICE_TEXT_CHECKBOX,
		SINGLE_CHOICE_IMAGE_RADIO,
		SINGLE_CHOICE_TEXT_DROPDOWN,
		SINGLE_CHOICE_TEXT_RADIO,
		SINGLE_CHOICE_TEXT_CHECKBOX,
		SINGLE_CHOICE_IMAGE_CHECKBOX,
		YES_NO,
		SHORT_ANSWER,
		DATE_TIME,
		STAR_RATING
	} 
	
	@Id
	@GeneratedValue
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private int id;
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private int guiOrder;
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private String question;
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private QUESTION_TYPES questionType;
	
	@Embedded
	@JsonProperty("questionContent")
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private QuestionContent questionContent;
	
	@ManyToOne
	@JsonView(View.Account.class)
	private Survey survey;
	
	@Transient
	@JsonView(View.Report.class)
	private String statistic;
	
	public Question() {
		
	}

	public Question(String question, QUESTION_TYPES questionType, QuestionContent questionContent) {
		this.question = question;
		this.questionType = questionType;
		this.questionContent = questionContent;
		
	}

	public int getGuiOrder() {
		return guiOrder;
	}

	public void setGuiOrder(int guiOrder) {
		this.guiOrder = guiOrder;
	}


	public QuestionContent getQuestionContent() {
		return questionContent;
	}

	public void setQuestionContent(QuestionContent questionContent) {
		this.questionContent = questionContent;
	}

	public Survey getSurvey() {
		return survey;
	}

	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public QUESTION_TYPES getQuestionType() {
		return questionType;
	}

	public void setQuestionType(QUESTION_TYPES questionType) {
		this.questionType = questionType;
	}

	public String getStatistic() {
		return statistic;
	}

	public void setStatistic(String statistic) {
		this.statistic = statistic;
	}


	
	
}
