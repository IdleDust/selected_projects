package edu.sjsu.cmpe275.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.sjsu.cmpe275.project.entities.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer>{
	public Question findQuestionByGuiOrderAndSurveyId(int guiOrder, int surveyId);
	
	public void deleteQuestionBySurveyId(int surveyId);
}
