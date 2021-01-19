package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	@Autowired
	private UserRepository UserRepository;

	@RequestMapping("/")
	public String home(Model model) 
	{
		model.addAttribute("title","Home- smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) 
	{
		model.addAttribute("title","About- smart Contact Manager");
		return "about";
	}
	@RequestMapping("/signup")
	public String signup(Model model) 
	{
		model.addAttribute("title","Register- smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//handle for registering user
	@RequestMapping(value = "/do_register", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, @RequestParam(value="agreement", defaultValue = "false") boolean agreement, Model model ,HttpSession session)
	{
	try {
		if(!agreement)
		{
			
			System.out.println("you have not agreed the terms and conditions");
			throw new Exception("you have not agreed the terms and conditions");
			
		}
		
		if(bindingResult.hasErrors())
		{
			System.out.println("ERROR "+ bindingResult.toString() );
			model.addAttribute("user",user);
		return "signup";	
		}
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		
		System.out.println("Agreement " + agreement);
		System.out.println("User " + user);
		
		User result = this.UserRepository.save(user);
		
		model.addAttribute("user", new User());
		session.setAttribute("message", new Message("Successfull Registered !!", "alert-success"));
		
        
		//model.addAttribute("user",user);
	} catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
		model.addAttribute("user", user);
		session.setAttribute("message", new Message("Something Went Wrong !!"+e.getMessage(), "alert-danger"));
		
		return "signup";
	}
		
		return "signup";
	}
}
