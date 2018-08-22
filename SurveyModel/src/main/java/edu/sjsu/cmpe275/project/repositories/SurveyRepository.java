package edu.sjsu.cmpe275.project.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.sjsu.cmpe275.project.entities.Survey;

public interface SurveyRepository extends JpaRepository<Survey, Integer>{
	public Survey findSurveyByLink(String link);
	
	@Query("select s from Survey s where survey_type = ?1")
	public List<Survey> findSurveyBySurveyType(int surveyType);
	
	public List<Survey> findSurveyByAccountId(int accountId);

}
