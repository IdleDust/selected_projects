package edu.sjsu.cmpe275.project.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import edu.sjsu.cmpe275.project.entities.AnswerQuestion;

public interface AnswerQuestionRepository extends JpaRepository<AnswerQuestion, Integer>{
	
	//for single choice
	@Query("select count(aq) from AnswerQuestion aq where answer_content = ?1 and question_id = ?2")
	public Long countByAnswerContentAndQuestionId(String answerContent, int questionId);
	
	//for short answer
	@Query("select aq from AnswerQuestion aq where question_id = ?1")
	public List<AnswerQuestion> findByQuestionId(int questionId);
	
	//for multiple choice
	@Query("select count(aq) from AnswerQuestion aq where question_id = ?1 and answer_content like %?2% ")
	public Long countByQuestionIdAndAnswerContentLike(int questionId, String answerContent);
}
