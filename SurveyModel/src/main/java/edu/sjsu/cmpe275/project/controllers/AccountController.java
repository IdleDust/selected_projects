package edu.sjsu.cmpe275.project.controllers;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonView;

import edu.sjsu.cmpe275.project.exception.CustomRestExceptionHandler;
import edu.sjsu.cmpe275.project.exception.ExceptionJSONInfo;
import edu.sjsu.cmpe275.project.View;
import edu.sjsu.cmpe275.project.entities.Account;
import edu.sjsu.cmpe275.project.repositories.AccountRepository;
import edu.sjsu.cmpe275.project.services.NotificationService;

@Controller
public class AccountController {
	
	@Autowired
	private AccountRepository accountRepo;
	
	@Autowired
	private NotificationService notifService;
	/*
	@GetMapping("/")
	public String showPage(Model model, @RequestParam(defaultValue="1")int page) {
		model.addAttribute("data", accountRepo.findAll(new PageRequest(page, 4)));
		return "index";
	}
	
	
	@GetMapping("/")
	public String showIndex() {
		
		return "index";
	}
	
	@GetMapping("/signup")
	public String showPage(Model model) {
		model.addAttribute("account", new Account());
		
		return "signup";
	}
	
	
	@PostMapping("/account")
	public String saveAccount(@Valid @ModelAttribute("account") Account account, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
            return "signup";
        }
		
		accountRepo.save(account);
		//send notification
		try {
			notifService.sendNotification(account.getEmail());
		}
		catch(MailException e) {
			//catch error
		}
		
		return "redirect:/";
	}
	*/
	
	@GetMapping("/api/get_user_id")
	@JsonView(View.Account.class)
	public ResponseEntity<Account> getUserId(@RequestParam("email") String email) {
		System.out.println(email);
		Account a = accountRepo.findAccountByEmail(email);
		if(a == null) {
			//return new ResponseEntity<Account>(HttpStatus.BAD_REQUEST);
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Wrong email or password!");
		}
		return new ResponseEntity<Account>(a, HttpStatus.OK);
	}
	
	@GetMapping("/account/{id}")
	@JsonView(View.Account.class)
	public ResponseEntity<Account> getAccount(@PathVariable int id){
		Account a = accountRepo.findById(id).orElse(null);
		if(a == null) {
			throw new CustomRestExceptionHandler(HttpStatus.NOT_FOUND, "Sorry, the requested account with id "+ id +" does not exist.");
		}
		return new ResponseEntity<Account>(a, HttpStatus.OK);
	}
	
	@GetMapping("/email_verify")
	public ResponseEntity<Object> getAccount(@RequestParam("email") String email){
		Random rand = new Random();
		int code = rand.nextInt(9000) + 1000;
		boolean status = false;
		String password = "00000000";
		if(accountRepo.findAccountByEmail(email) == null) {
			Account accountUnverified = new Account(email, password, code, status);
			accountRepo.save(accountUnverified);
			notifService.sendVerification(email, code);
			
		}
		else {
			// throw exception: email already exist
			ExceptionJSONInfo info = new ExceptionJSONInfo();
			info.setCode(400);
			info.setMsg("Email "+ email +" already exist.");
			return new ResponseEntity<Object>(info, HttpStatus.BAD_REQUEST);
		}
		//return new ResponseEntity<Object>(HttpStatus.OK);
		ExceptionJSONInfo info = new ExceptionJSONInfo();
		info.setCode(200);
		info.setMsg("Verification code has been sent. Please check your email.");
		return new ResponseEntity<Object>(info, HttpStatus.OK);
	}
	
	@PostMapping("/signup")
	@JsonView(View.Account.class)
	public ResponseEntity<Account> saveVerifiedAccount(@RequestParam("email") String email,
													   @RequestParam("code") int code,
													   @RequestParam("password") String password){
		Account accountVerified = accountRepo.findAccountByEmail(email);
		if(accountVerified != null && code == accountVerified.getCode()) {
			accountVerified.setPassword(password);
			accountVerified.setStatus(true);
			accountRepo.save(accountVerified);
			notifService.sendNotification(email);
		}
		else {
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Wrong verification code!");
		}
		// to do redirect
		return new ResponseEntity<Account>(accountVerified,HttpStatus.OK);
	}

	
	@GetMapping("/login")
	@JsonView(View.Account.class)
	public ResponseEntity<Account> login(@RequestParam("email") String email,
			   			@RequestParam("password") String password){
		Account a = accountRepo.findAccountByEmail(email);
		if(a == null || ! password.equals(a.getPassword()) || a.isStatus() == false) {
			//return new ResponseEntity<Account>(HttpStatus.BAD_REQUEST);
			throw new CustomRestExceptionHandler(HttpStatus.BAD_REQUEST, "Wrong email or password!");
		}
		
		return new ResponseEntity<Account>(a, HttpStatus.OK);					
	}
	
}
