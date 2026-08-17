package com.example.hrms.controller;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.hrms.dto.ResponseDto;
import com.example.hrms.model.JobApplication;
import com.example.hrms.repo.JobApplicationRepo;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hrms.model.JobInfo;
import com.example.hrms.model.Response;
import com.example.hrms.model.User;
import com.example.hrms.repo.JobInfoRepo;
import com.example.hrms.repo.ResponseRepo;
import com.example.hrms.repo.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	@Autowired
	JobInfoRepo jrepo;
	
	@Autowired
	UserRepo urepo;
	
	@Autowired
	JobApplicationRepo apprepo;
	
	@Autowired
	ResponseRepo rrepo;
	
	
	@GetMapping("/user/userdash")
	public String userDashboard(HttpSession session,
	                            Model model) {

	    if(session.getAttribute("user")==null){

	        return "redirect:/login";

	    }

	    User user=(User)session.getAttribute("user");

	    List<JobInfo> jobs=jrepo.findAll();
	    
	    Map<Integer, String> jobTitles = new HashMap<>();

	    for (JobInfo job : jobs) {
	        jobTitles.put(job.getId(), job.getTitle());
	    }

	    model.addAttribute("jobTitles", jobTitles);

	    model.addAttribute("jobs",jobs);

	    List<JobApplication> applications= apprepo.findByUserid(user.getUserid());

	    model.addAttribute("applications",applications);

	    model.addAttribute(
	            "appliedCount",
	            apprepo.countByUserid(user.getUserid()));

	    model.addAttribute(
	            "pendingCount",
	            apprepo.countByUseridAndStatus(
	                    user.getUserid(),
	                    "Pending"));

	    model.addAttribute(
	            "acceptedCount",
	            apprepo.countByUseridAndStatus(
	                    user.getUserid(),
	                    "Accepted"));

	    model.addAttribute(
	            "rejectedCount",
	            apprepo.countByUseridAndStatus(
	                    user.getUserid(),
	                    "Rejected"));

	    return "user/userdash";

	}
	
	@GetMapping("user/viewjobs")
	public String viewjobs(HttpSession session,Model model) {
		if(session.getAttribute("user")==null) {
			return"redirect:/login";
			}
		List<JobInfo> jinfo=jrepo.findAll();
		model.addAttribute("jinfo", jinfo);
		return "user/viewjobs";
	}
	@GetMapping("/user/changepwd")
    public String changePwd(HttpSession session) {
		if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
   	 return "user/changepwd";
   	 
	}
	@GetMapping("user/logout")
    public String logout(HttpSession session) {
		if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
   	session.removeAttribute("user");
   	return "redirect:/login";
   	
   	 
	}
	@PostMapping("/user/changepwd")
	public String changePwd(HttpSession session, HttpServletRequest request, RedirectAttributes attrib) {
		System.out.println("Change Password POST method called");
		if (session.getAttribute("user") == null) {
        return "redirect:/login";
	}
	String oldpassword=request.getParameter("oldpassword");
	String newpassword=request.getParameter("newpassword");
	String confirmpassword=request.getParameter("confirmpassword");
	if(!newpassword.equals(confirmpassword)) {
		attrib.addFlashAttribute("msg", "New password and confirm password are not equal");
		return "redirect:/user/changepwd";
	}
	try {
		User user=(User) session.getAttribute("user");
		if(!user.getPassword().equals(oldpassword)) {
			System.out.println("Session Password : " + user.getPassword());
			System.out.println("Entered Password : " + oldpassword);
			attrib.addFlashAttribute("msg","Old password is not matched");
			return "redirect:/user/changepwd";
		}
		user.setPassword(newpassword);
		urepo.save(user);
		return "redirect:/user/logout";
	}
	catch(Exception e) {
		attrib.addFlashAttribute("msg", "User Id is not matched");
		return "redirect:/user/changepwd";
	}
	}
	@GetMapping("/user/giveresponse")
	public String showGiveResponse(HttpSession session, Model model)
	{
		if(session.getAttribute("user")==null)
		{	
			return "redirect:/login";
		}
		ResponseDto rdto = new ResponseDto();
		model.addAttribute("rdto",rdto);
		return "user/giveresponse";
	
	}
	@PostMapping("/user/giveresponse")
    public String giveResponse(HttpSession session, @ModelAttribute ResponseDto rdto, RedirectAttributes attrib) {
    	if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
   	 Response response=new Response();
   	response.setResponsetype(rdto.getResponsetype());
   	response.setSubject(rdto.getSubject());
   	response.setResponsetext(rdto.getResponsetext());
   	java.util.Date dt=new java.util.Date();
   	SimpleDateFormat sdf=new SimpleDateFormat("dd/mm/yyyy");
   	String posteddate=sdf.format(dt);
   	response.setPosteddate(posteddate);
   	User user=(User) session.getAttribute("user");
   	response.setName(user.getName());
   	response.setContactno(user.getContactno());
   	rrepo.save(response);
   	attrib.addFlashAttribute("msg", "Your response is submitted");
   	
    	return "redirect:/user/giveresponse";
    }
	@GetMapping("/user/viewprofile")
	public String viewProfile(HttpSession session, Model model) {

	    if (session.getAttribute("user") == null) {
	        return "redirect:/login";
	    }

	    User user = (User) session.getAttribute("user");

	    model.addAttribute("user", user);   // ⭐ Missing line

	    return "user/viewprofile";
	}
}
	