package edu.sjsu.cmpe275.project.controllers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.View;
import edu.sjsu.cmpe275.project.entities.Answer;
import edu.sjsu.cmpe275.project.entities.AnswerQuestion;
import edu.sjsu.cmpe275.project.entities.Invitation;
import edu.sjsu.cmpe275.project.entities.Question;
import edu.sjsu.cmpe275.project.entities.Question.QUESTION_TYPES;
import edu.sjsu.cmpe275.project.entities.Survey;
import edu.sjsu.cmpe275.project.entities.Survey.SURVEY_TYPES;
import edu.sjsu.cmpe275.project.exception.CustomRestExceptionHandler;
import edu.sjsu.cmpe275.project.exception.ExceptionJSONInfo;
import edu.sjsu.cmpe275.project.repositories.AnswerQuestionRepository;
import edu.sjsu.cmpe275.project.repositories.AnswerRepository;
import edu.sjsu.cmpe275.project.repositories.InvitationRepository;
import edu.sjsu.cmpe275.project.repositories.QuestionRepository;
import edu.sjsu.cmpe275.project.repositories.SurveyRepository;

@Controller
public class AnswerController {

	@Autowired
	private AnswerRepository answerRepo;
	
	@Autowired
	private SurveyRepository surveyRepo;
	
	@Autowired
	private AnswerQuestionRepository aqRepo;
	
	@Autowired
	private QuestionRepository questionRepo;
	
	@Autowired
	private InvitationRepository invitationRepo;
	
	@PostMapping(value = "/answer/{uuid}")
	//@JsonView(View.Answer.class)
	public ResponseEntity<?> saveAnswer(@PathVariable("uuid") String uuid, @RequestBody Answer answer){
		
		// according to the type
		// 1 general or anonymous, skip the check
		// 2 open or close, check if the email has already sumbitted
		// 3 close check if the link has already submitted
		
		Survey s = surveyRepo.findSurveyByLink(uuid);
		//type check
		if(s == null) {
			Invitation invitation = invitationRepo.findInvitationByLink(uuid);
			if(invitation != null) {
				s = invitation.getSurvey();
			}	
			else{
				throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested survey does not exist.");
			}
		}
		SURVEY_TYPES type = s.getSurveyType();
		
		//check time if is expired
		Date currentDate = new Date();
		
		if(s.getStartTime().after(currentDate) || s.getEndTime().before(currentDate)) {
			//return new ResponseEntity<Answer>(HttpStatus.BAD_REQUEST);
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Sorry, survey time is out.");
		}
		//check if the link is corresponding to right email
		if(type == SURVEY_TYPES.CLOSED_INVITATION) {
			Invitation invitation = invitationRepo.findInvitationByLink(uuid);
			if(! answer.getEmail().equals(invitation.getEmail())) {
				throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Sorry, wrong email.");
			}
		}
		
		if(type == SURVEY_TYPES.CLOSED_INVITATION || type == SURVEY_TYPES.OPEN_UNIQUE) {
			
			Answer a = answerRepo.findAnswerByEmailAndSurveyId(answer.getEmail(), s.getId());
			if(a != null) {
				//return new ResponseEntity<Answer>(HttpStatus.BAD_REQUEST);
				ExceptionJSONInfo info = new ExceptionJSONInfo();
				info.setCode(400);
				info.setMsg("Email "+ answer.getEmail() +" already submit an answer.");
				return new ResponseEntity<Object>(info, HttpStatus.BAD_REQUEST);
			}
		}
		
		
		
		// the answer is valid, need to insert into DB
		// 1 get the survey by uuid
		// 2 setup relation between answer and answer_question (answver_question.set(answer) only)
		// 3 setup relation between survey and answer 
		// 4 set up relation between question and answer_question
		// 5 save the necessaries.
		
		//List<AnswerQuestion> aqList = answer.getAq();
		
		for(AnswerQuestion aq : new ArrayList<AnswerQuestion>(answer.getAq())) {
			Question q = questionRepo.findById(aq.getQuestionId()).orElse(null);
			aq.setQuestion(q);
			aq.setAnswer(answer);
			
			aqRepo.save(aq);
		}
		answer.setSurvey(s);
		answerRepo.save(answer);
		ExceptionJSONInfo info = new ExceptionJSONInfo();
		info.setCode(200);
		info.setMsg("Answer has been submitted.");
		return new ResponseEntity<Object>(info, HttpStatus.OK);
	}
	
	//get report for a certain survey
	@GetMapping(value="/account/{accountId}/report")
	@JsonView(View.Report.class)
	public ResponseEntity<?> getReport(@PathVariable("accountId") int accountId,
									   @RequestParam("surveyId") int surveyId){
		/*
		 * 1 find survey by surveyId
		 * 2 get question list by survey
		 * 3 for each question in question list, split question content
		 * 4 for each question content, find related answer_question, if exist, count ++
		 */
		
		Survey s = surveyRepo.findById(surveyId).orElse(null);
		if(s == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested survey does not exist.");
		}
		Long countA = answerRepo.countBySurveyId(surveyId);
		s.setParticipantNum(countA);
		s.setParticipationRate(null);
		if(s.getSurveyType() == SURVEY_TYPES.CLOSED_INVITATION) {
			Long invitationNum = invitationRepo.countBySurveyId(surveyId);
//			DecimalFormat df = new DecimalFormat("##.##");
			String rate = String.valueOf((countA * 100.0)/invitationNum) + "%";
//			s.setParticipationRate(df.format((countA * 100.0)/invitationNum)+"%");
			s.setParticipationRate(invitationNum == 0 ? "0%" : rate);
		}
		List<Question> questionList = s.getQuestions();
		for(Question q : questionList) {
			
			QUESTION_TYPES questionType = q.getQuestionType();
			switch(questionType) {
//				case SINGLE_CHOICE_TEXT:
//				case SINGLE_CHOICE_IMAGE:
//				case SINGLE_CHOICE_DROPDOWN:
//				case SINGLE_CHOICE_RADIO:
//				case SINGLE_CHOICE_CHECKBOX:

				case SINGLE_CHOICE_IMAGE_RADIO:
				case SINGLE_CHOICE_TEXT_DROPDOWN:
				case SINGLE_CHOICE_TEXT_RADIO:
				case SINGLE_CHOICE_TEXT_CHECKBOX:
				case SINGLE_CHOICE_IMAGE_CHECKBOX:
				case YES_NO:
				//case DATE_TIME:
				//case STAR_RATING:
					String questionContentStr = q.getQuestionContent().getQuestionContent();
					String[] choiceArray = questionContentStr.split(";");//or any other separator
					ArrayList<String> choiceList = new ArrayList<String>(Arrays.asList(choiceArray));
					String totalS = "";
					for(String choice : choiceList) {
						Long countS = aqRepo.countByAnswerContentAndQuestionId(choice, q.getId());
						totalS = totalS + countS + ";";
					}
					q.setStatistic(totalS);
					break;
//				case MULTIPLE_CHOICE_TEXT:
//				case MULTIPLE_CHOICE_IMAGE:
//				case MULTIPLE_CHOICE_CHECKBOX:
				case MULTIPLE_CHOICE_IMAGE_CHECKBOX:
				case MULTIPLE_CHOICE_TEXT_CHECKBOX:
					String qcStr = q.getQuestionContent().getQuestionContent();
					String[] selectionArray = qcStr.split(";");//or any other separator
					ArrayList<String> selectionList = new ArrayList<String>(Arrays.asList(selectionArray));
					String totalM = "";
					for(String selection : selectionList) {
						Long countM = aqRepo.countByQuestionIdAndAnswerContentLike(q.getId(), selection);
						totalM = totalM + countM + ";";
					}
					q.setStatistic(totalM);
					break;
				case SHORT_ANSWER:
				case DATE_TIME:
				case STAR_RATING:
					List<AnswerQuestion> aqList = aqRepo.findByQuestionId(q.getId());
					String allAnswer = "";
					for(AnswerQuestion aq : aqList) {
						allAnswer = allAnswer + aq.getAnswerContent() + ";";
					}
					q.setStatistic(allAnswer);
					break;
					default:
			}
		}
		return new ResponseEntity<Survey>(s, HttpStatus.OK);
	}
}
