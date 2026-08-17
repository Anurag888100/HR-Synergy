package com.example.hrms.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import com.example.hrms.dto.AdminInfoDto;
import com.example.hrms.dto.EnquiryDto;
import com.example.hrms.dto.JobApplicationDto;
import com.example.hrms.dto.UserDto;
import com.example.hrms.model.AdminInfo;
import com.example.hrms.model.Enquiry;
import com.example.hrms.model.JobApplication;
import com.example.hrms.model.JobInfo;
import com.example.hrms.model.User;
import com.example.hrms.repo.AdminInfoRepo;
import com.example.hrms.repo.EnquiryRepo;
import com.example.hrms.repo.JobApplicationRepo;
import com.example.hrms.repo.JobInfoRepo;
import com.example.hrms.repo.UserRepo;

@Controller
public class MainController {
	@Autowired
	EnquiryRepo erepo;// variable of enquiryRepo interface

	@Autowired
	UserRepo urepo;

	@Autowired
	AdminInfoRepo airepo;
	
	@Autowired
	JobApplicationRepo apprepo;
	
	@Autowired
	JobInfoRepo jrepo;

	@GetMapping("/")
	public String showIndex() {
		return "index";
	}

	@GetMapping("/aboutus")
	public String showAboutUs() {
		return "user/aboutus";
	}

	@GetMapping("/registration")
	public String showRegistration(Model model) {
		UserDto udto = new UserDto();
		model.addAttribute("udto", udto);
		return "registration";
	}

	@PostMapping("/registration")
	public String saveJobSeeker(@ModelAttribute UserDto udto,
	                            RedirectAttributes attrib) {

	    User existingUser = urepo.findByEmailaddress(udto.getEmailaddress());

	    if (existingUser != null) {

	        attrib.addFlashAttribute("msg",
	                "This email is already registered.");

	        return "redirect:/registration";
	    }

	    User user = new User();

	    user.setName(udto.getName());
	    user.setGender(udto.getGender());
	    user.setContactno(udto.getContactno());
	    user.setEmailaddress(udto.getEmailaddress());
	    user.setPassword(udto.getPassword());
	    user.setQualification(udto.getQualification());
	    user.setExperience(udto.getExperience());
	    user.setKeyskills(udto.getKeyskill());
	    user.setAddress(udto.getAddress());

	    urepo.save(user);

	    attrib.addFlashAttribute("msg",
	            "Registration Successful!");

	    return "redirect:/login";
	}
	
	@GetMapping("/login")
	public String showLogin() {
		return "login";
	}

	@PostMapping("/login")
	public String showDashboard(@RequestParam String emailaddress, String password, HttpSession session,
			RedirectAttributes redirectAttributes) {
		Optional<User> user = urepo.findByEmailaddressAndPassword(emailaddress, password);
		if (user.isPresent()) {
			session.setAttribute("user", user.get());
			return "redirect:/user/userdash";
		}
		redirectAttributes.addFlashAttribute("msg", "Invalid Email and Password");
		return "redirect:/login";
	}
	@GetMapping("/contactus")
	public String contactUs(Model model) {

	    model.addAttribute("edto", new EnquiryDto());

	    return "contactus";
	}

	@PostMapping("/contactus")
	public String saveEnquiry(@ModelAttribute EnquiryDto edto, RedirectAttributes attrib) {
		Enquiry enq = new Enquiry();
		enq.setName(edto.getName());
		enq.setAddress(edto.getAddress());
		enq.setContactno(edto.getContactno());
		enq.setEmailaddress(edto.getEmailaddress());
		enq.setEnquirytext(edto.getEnquirytext());
		erepo.save(enq);
		attrib.addFlashAttribute("msg", "Enquiry is saved");
		return "redirect:/contactus";
	}



	@GetMapping("/adminlogin")
	public String showAdminLogin(Model model) {
		AdminInfoDto aidto = new AdminInfoDto();
		model.addAttribute("aidto", aidto);
		return "adminlogin";

	}

	@PostMapping("/adminlogin")
	public String adminlogin(@ModelAttribute AdminInfoDto aidto, RedirectAttributes attrib, HttpSession session) {
		String adminid = aidto.getAdminid();
		String password = aidto.getPassword();

		try {
			AdminInfo admin = airepo.findById(adminid).get();

			if (admin.getPassword().equals(password)) {
				//attrib.addFlashAttribute("msg", "welcome back admin");
				session.setAttribute("admin", admin);
				return "redirect:/admin/admindashboard";
			} else {
				attrib.addFlashAttribute("msg", "Invalid adminid/ password");
				return "redirect:/adminlogin";
			}
		} catch (Exception e) {
			attrib.addFlashAttribute("msg", "Invalid adminid/ password");
		
		return "redirect:/adminlogin";
	}
	}
	
	@GetMapping("/joinus")
	public String joinUs(Model model) {

	    model.addAttribute("jobs", jrepo.findAll());

	    return "joinus";
	}
	@GetMapping("/jobs")
	public String showJobs(Model model,
	                       HttpSession session) {

	    List<JobInfo> jobs = jrepo.findAll();

	    model.addAttribute("jobs", jobs);

	    if(session.getAttribute("user") != null){

	        User user = (User) session.getAttribute("user");

	        List<Integer> appliedJobs = new ArrayList<>();

	        for(JobInfo job : jobs){

	            if(apprepo.existsByUseridAndJobid(
	                    user.getUserid(),
	                    job.getId())){

	                appliedJobs.add(job.getId());

	            }

	        }

	        model.addAttribute("appliedJobs", appliedJobs);

	    }

	    return "jobs";
	}
	
	@GetMapping("/applyjob")
	public String applyJob(@RequestParam int id,
	                       HttpSession session,
	                       Model model) {

	    if (session.getAttribute("user") == null) {
	        return "redirect:/login";
	    }

	    User user = (User) session.getAttribute("user");

	    JobApplicationDto dto = new JobApplicationDto();

	    dto.setFullname(user.getName());
	    dto.setEmail(user.getEmailaddress());
	    dto.setContact(user.getContactno());
	    dto.setQualification(user.getQualification());
	    dto.setExperience(user.getExperience());

	    // Fetch the selected job
	    JobInfo job = jrepo.findById(id).orElse(null);

	    model.addAttribute("adto", dto);
	    model.addAttribute("job", job);
	    model.addAttribute("jobid", id);

	    return "applyjob";
	}
	
	@PostMapping("/applyjob")
	public String saveApplication(
	        @ModelAttribute JobApplicationDto adto,
	        @RequestParam("resume") MultipartFile file,
	        @RequestParam int jobid,
	        HttpSession session,
	        RedirectAttributes attrib) {

	    if (session.getAttribute("user") == null) {
	        return "redirect:/login";
	    }

	    User user = (User) session.getAttribute("user");

	    JobApplication app = new JobApplication();

	    app.setUserid(user.getUserid());
	    app.setJobid(jobid);

	    app.setFullname(adto.getFullname());
	    app.setEmail(adto.getEmail());
	    app.setContact(adto.getContact());
	    app.setQualification(adto.getQualification());
	    app.setExperience(adto.getExperience());
	    app.setCoverletter(adto.getCoverletter());

	    app.setStatus("Pending");
	    app.setApplieddate(new Date().toString());

	    try {

	        if (!file.isEmpty()) {

	            String filename =
	                    System.currentTimeMillis() + "_"
	                    + file.getOriginalFilename();

	            Path uploadPath =
	                    Paths.get("src/main/resources/static/resumes");

	            Files.createDirectories(uploadPath);

	            Files.copy(
	                    file.getInputStream(),
	                    uploadPath.resolve(filename),
	                    StandardCopyOption.REPLACE_EXISTING);

	            app.setResume(filename);

	        }

	    } catch (Exception e) {

	        e.printStackTrace();

	    }

	    apprepo.save(app);

	    attrib.addFlashAttribute(
	            "msg",
	            "Application Submitted Successfully!");

	    return "redirect:/jobs";
	}
}