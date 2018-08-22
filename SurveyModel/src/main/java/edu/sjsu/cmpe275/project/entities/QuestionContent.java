package edu.sjsu.cmpe275.project.entities;

import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;


@Embeddable
public class QuestionContent {
	
	@JsonView({View.Survey.class,View.Account.class,View.Question.class})
	private String questionContent;
	
	
	
	public QuestionContent() {
		
	}

	
	public QuestionContent(String questionContent) {
		
		this.questionContent = questionContent;
		
	}

	public String getQuestionContent() {
		return questionContent;
	}

	public void setQuestionContent(String questionContent) {
		this.questionContent = questionContent;
	}
	
}
