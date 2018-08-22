package edu.sjsu.cmpe275.project.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;
import edu.sjsu.cmpe275.project.entities.Question;
import edu.sjsu.cmpe275.project.entities.Question.QUESTION_TYPES;
import edu.sjsu.cmpe275.project.entities.Survey;
import edu.sjsu.cmpe275.project.repositories.QuestionRepository;
import edu.sjsu.cmpe275.project.repositories.SurveyRepository;

@Controller
public class QuestionController {
	
	@Autowired
	private QuestionRepository questionRepo;

	@Autowired
	private SurveyRepository surveyRepo;
	
	@JsonView(View.Question.class)
	@GetMapping(value="/question/{id}")
	public ResponseEntity<Question> getQuestion(){
		Question q = new Question();
		q.setQuestion("hello");
		q.setQuestionType(QUESTION_TYPES.SHORT_ANSWER);
		return new ResponseEntity<Question>(q, HttpStatus.OK);
	}

	@JsonView(View.Question.class)
	@PostMapping(value="/question")
	public ResponseEntity<Question> saveQuestion(@RequestBody Question question){
		questionRepo.save(question);
			
		return new ResponseEntity<Question>(question, HttpStatus.OK);
	}
	
	@JsonView(View.Question.class)
	@PostMapping(value="/addquestion/{surveyId}")
	public ResponseEntity<Question> createQuestion(@PathVariable("surveyId") int surveyId,
												   @RequestBody Question question){
		Survey s = surveyRepo.findById(surveyId).orElse(null);
		question.setSurvey(s);
		s.addQuestion(question);
		questionRepo.save(question);
		return new ResponseEntity<Question>(question, HttpStatus.OK);
	}
}
