package edu.sjsu.cmpe275.project.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;


@Entity
public class AnswerQuestion {
	
	@Id
	@GeneratedValue
	private int id;
	
	@ManyToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name="question_id")
	private Question question;
	
	@ManyToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name="answer_id")
	private Answer answer;
	@JsonView(View.Answer.class)
	private String answerContent;
	
	@Transient
	private int questionId;
	
		
	public AnswerQuestion() {
		
	}

	public AnswerQuestion(String answerContent) {
		this.answerContent = answerContent;
	}

	
	public int getId() {
		return id;
	}

	public void setAqId(int id) {
		this.id = id;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Answer getAnswer() {
		return answer;
	}

	public void setAnswer(Answer answer) {
		this.answer = answer;
	}

	public String getAnswerContent() {
		return answerContent;
	}

	public void setAnswerContent(String answerContent) {
	
		this.answerContent = answerContent;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	

}
