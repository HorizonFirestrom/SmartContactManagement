package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;



@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired 
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model , Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME " + userName);
		User user =  userRepository.getUserByName(userName);
		System.out.println("User " + user );
		model.addAttribute("user",user);
		
	}
	
	

	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		
		
		return "normal/user_dashboard";
	}
	
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session) {
		
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByName(name);
		
		//processing and uploading file...
		
		if(file.isEmpty())
		{
			//if the file empty then try our message
			System.out.println("Image file is empty");
			contact.setImage("contact.png");
			
		}
		else {
			//upload the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/image").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
			
		}
		
		
		
		contact.setUser(user);
		
		
		
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("Added to database");
		
		//message success
		session.setAttribute("message", new Message("Your contact is added !! Add More", "success"));
		
		
		System.out.println("Data: " + contact);
		
		
		}
		catch (Exception e) {
			
			// TODO: handle exception
			//
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			
			//error message
			session.setAttribute("message", new Message("Something went wrong !! Try Again !!!", "danger"));
			
		}
		return "normal/add_contact_form";
	}
	
	//show contact handler
	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page")Integer page ,Model m, Principal principal)
	{
		m.addAttribute("title","show User Contacts" );
		
		//getting the list of users by current user
		String userName = principal.getName();
		User user = this.userRepository.getUserByName(userName);
		
		
		//current page
		//contact per page - 10
		PageRequest pageable = PageRequest.of(page, 10);
		
	    Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		
	    
	    
	    m.addAttribute("contacts",contacts);
	    m.addAttribute("currentPage", page);
	    
	    m.addAttribute("totalPages", contacts.getTotalPages());
		
		
	    
	    return "normal/show_contact";
		
	}
	
	//showing particular contact detail
	@RequestMapping("/contact/{cid}")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal principal)
	{
		System.out.println("CID " +cid);
		
		Optional<Contact> contactOptionals = this.contactRepository.findById(cid);
		
		Contact contact = contactOptionals.get();
		
		
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByName(userName);
		if(user.getId() == contact.getUser().getId())
		{
			
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
			
		}
		
		
		
		
		
		
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal, HttpSession session)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact = contactOptional.get(); 
		
		//detaching contact from  
		contact.setUser(null);
		
		
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
			this.contactRepository.delete(contact);
			
		
		
		session.setAttribute("message", new Message("Contact deleted successfully...","success"));
		
		return "redirect:/user/show-contact/0";
		
	}
	
}
