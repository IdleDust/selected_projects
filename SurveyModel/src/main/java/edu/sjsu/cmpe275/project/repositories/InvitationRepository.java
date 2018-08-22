package edu.sjsu.cmpe275.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.sjsu.cmpe275.project.entities.Invitation;


public interface InvitationRepository extends JpaRepository<Invitation, Integer>{

	public Invitation findInvitationByLink(String link);
	
	@Query("select count(i) from Invitation i where survey_id = ?1")
	public Long countBySurveyId(int surveyId);
}
