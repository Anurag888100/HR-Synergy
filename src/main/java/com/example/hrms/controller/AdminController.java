package com.example.hrms.controller;
import com.example.hrms.model.JobApplication; 
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.RedirectAttributesMethodArgumentResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.hrms.dto.JobInfoDto;
import com.example.hrms.model.AdminInfo;
import com.example.hrms.model.Enquiry;
import com.example.hrms.model.JobInfo;
import com.example.hrms.model.Response;
import com.example.hrms.model.User;
import com.example.hrms.repo.AdminInfoRepo;
import com.example.hrms.repo.AppliedJobRepo;
import com.example.hrms.repo.EnquiryRepo;
import com.example.hrms.repo.JobApplicationRepo;
import com.example.hrms.repo.JobInfoRepo;
import com.example.hrms.repo.ResponseRepo;
import com.example.hrms.repo.UserRepo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController 
{
	@Autowired
	JobInfoRepo jrepo;

	@Autowired
	UserRepo urepo;
	
	final EnquiryRepo erepo;
	
	@Autowired
	AdminInfoRepo airepo;
	
	@Autowired
	JobApplicationRepo apprepo;
	
	@Autowired
	ResponseRepo rrepo;

	@Autowired
	 JobInfoRepo jobInfoRepo;
	
	AdminController(EnquiryRepo erepo) {
		this.erepo = erepo;
	}

	@GetMapping("/admin/admindashboard")
	public String showAdminDashboard(HttpSession session, Model model) {

	    if (session.getAttribute("admin") == null) {
	        return "redirect:/adminlogin";
	    }

	    // Statistics
	    model.addAttribute("totalUsers", urepo.count());
	    model.addAttribute("totalJobs", jrepo.count());
	    model.addAttribute("totalApplications", apprepo.count());
	    model.addAttribute("totalEnquiries", erepo.count());

	    model.addAttribute("totalFeedback",
	            rrepo.findByResponsetype("feedback").size());

	    model.addAttribute("totalComplaints",
	            rrepo.findByResponsetype("complaint").size());
	    model.addAttribute("pendingApplications",
	            apprepo.countByStatus("Pending"));

	    model.addAttribute("acceptedApplications",
	            apprepo.countByStatus("Accepted"));

	    model.addAttribute("rejectedApplications",
	            apprepo.countByStatus("Rejected"));
	    model.addAttribute("popularJobs", apprepo.findMostAppliedJobs());
	    // Application Status
	    model.addAttribute("pendingApplications",
	            apprepo.countByStatus("Pending"));

	    model.addAttribute("acceptedApplications",
	            apprepo.countByStatus("Accepted"));

	    model.addAttribute("rejectedApplications",
	            apprepo.countByStatus("Rejected"));

	    // Tables
	    model.addAttribute("recentApplications", apprepo.findAll());
	    model.addAttribute("recentEnquiries", erepo.findAll());
	    model.addAttribute("popularJobs", apprepo.findMostAppliedJobs());

	    return "admin/admindashboard";
	}
	@GetMapping("admin/jobseeker")
	public String viewUsers(HttpSession session, Model model) {
		if (session.getAttribute("admin") == null) {
			return "redirect:/adminlogin";
		}
		List<User> users = urepo.findAll();
		model.addAttribute("users", users);
		return "admin/jobseeker";
	}

	@GetMapping("/admin/logout")
	public String logout(HttpSession session) {
		if (session.getAttribute("admin") == null) {
			return "redirect:/adminlogin";

		}
		session.invalidate();
		return "redirect:/adminlogin";
	}

	@GetMapping("/admin/postjob")
	public String showPostJob(Model model, HttpSession session) {
		if (session.getAttribute("admin") == null) {
			return "redirect:/adminlogin";
		}
		JobInfoDto jdto = new JobInfoDto();
		model.addAttribute("jdto", jdto);
		return "admin/postjob";
	}
	
@PostMapping("/admin/postjob")
public String savJob(@ModelAttribute JobInfoDto jdto, HttpSession session, RedirectAttributes attrib)
{
	if (session.getAttribute("admin") == null) {
		return "redirect:/adminlogin";
	}
	JobInfo ji = new JobInfo();
	ji.setTitle(jdto.getTitle());
	ji.setDescription(jdto.getDescription());
	ji.setLocation(jdto.getLocation());
	ji.setSalary(jdto.getSalary());
	ji.setJobtype(jdto.getJobtype());
	ji.setLastdate(jdto.getLastdate());
	String posteddate= new Date().toString();
	ji.setPosteddate(posteddate);
	jrepo.save(ji);
	attrib.addFlashAttribute("msg", "Job Details is Posted");
	return "redirect:/admin/postedjobs";
}
@GetMapping("/admin/postedjobs")
public String showPostedJobs(HttpSession session, Model model)
{
    if(session.getAttribute("admin")==null)
    {
        return "redirect:/adminlogin";
    }

    List<JobInfo> jobs = jrepo.findAll();

    model.addAttribute("jobs", jobs);

    return "admin/postedjobs";
}
@GetMapping("/admin/enquiries")
public String viewEnquiries(HttpSession session, Model model) {

    if (session.getAttribute("admin") == null) {
        return "redirect:/adminlogin";
    }

    List<Enquiry> enq= erepo.findAll();

    model.addAttribute("enq", enq);

    return "admin/enquiries";
}
@GetMapping("/admin/changeadminpwd")
public String changeAdminPassword(HttpSession session)
{
	if(session.getAttribute("admin")==null)
	{
		
	
	
		return "redirect:/adminlogin";
	}
	return "admin/changeadminpwd";
}
@PostMapping("/admin/changeadminpwd")
public String changeAdminPwd(HttpSession session, HttpServletRequest request, RedirectAttributes attrib)
{
	if(session.getAttribute("admin")==null)

{
		return "redirect:/adminlogin";
		}
	String oldpassword= request.getParameter("olderpassword");
	String newpassword = request.getParameter("newpassword");
	String confirmpassword=request.getParameter("confirmpassword");
	if(!newpassword.equals(confirmpassword))
	{
		attrib.addFlashAttribute("msg", "Newpassword and Confirmpassword are not machting");
		return "redirect:/admin/changeadminpwd";
	}
	try
	{
		AdminInfo admin = (AdminInfo)session.getAttribute("admin");
		if(!admin.getPassword().equals(oldpassword)) {
			attrib.addFlashAttribute("msg", "Oldpassword is not machting");
			return "redirect:/admin/changeadminpwd";
		}
		admin.setPassword(newpassword);
		airepo.save(admin);
		return "redirect:/admin/adminlogout";
	}
	catch(Exception e)
	{
		attrib.addFlashAttribute("msg", "AdminId not matched");
		return "redirect:/admin/changeadminpwd";
	}
}
@GetMapping("/admin/applicants")
public String viewApplicants(@RequestParam int jobid,
                             HttpSession session,
                             Model model) {

    if (session.getAttribute("admin") == null) {
        return "redirect:/adminlogin";
    }

    model.addAttribute(
            "applications",
            apprepo.findByJobid(jobid));

    return "admin/applicants";
}
@GetMapping("/admin/viewfeedback")
public String viewFeedback(Model model,HttpSession session) {
	if (session.getAttribute("admin") == null) {
        return "redirect:/adminlogin";
}
	List<Response> feed=rrepo.findByResponsetype("feedback");
	model.addAttribute("feed", feed);
	return "/admin/viewfeedback";
}
@GetMapping("/admin/viewcomplaint")
public String viewComplaint(Model model,HttpSession session) {
	if (session.getAttribute("admin") == null) {
        return "redirect:/adminlogin";
}
	List<Response> comp=rrepo.findByResponsetype("complaint");
	model.addAttribute("comp",comp);
	return "/admin/viewcomplaint";
}

@GetMapping("/admin/deleteenquiry/{id}")
public String deleteEnquiry(@PathVariable("id") int id,
                            HttpSession session,
                            RedirectAttributes attrib) {

    if (session.getAttribute("admin") == null) {
        return "redirect:/adminlogin";
    }

    if (erepo.existsById(id)) {
        erepo.deleteById(id);
        attrib.addFlashAttribute("msg", "Enquiry deleted successfully.");
    } else {
        attrib.addFlashAttribute("msg", "Enquiry not found.");
    }

    return "redirect:/admin/enquiries";
}
@Autowired
private JobApplicationRepo jarepo;

@GetMapping("/admin/applicants/{jobid}")
public String viewApplicants(@PathVariable int jobid, Model model) {

    List<JobApplication> applicants = jarepo.findByJobid(jobid);

    model.addAttribute("applications", applicants);

    return "admin/applicants";
}
@PostMapping("/admin/deletejob")
public String deleteJob(@RequestParam("id") int id,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

    if (session.getAttribute("admin") == null) {
        return "redirect:/adminlogin";
    }

    jobInfoRepo.deleteById(id);

    redirectAttributes.addFlashAttribute("msg", "Job deleted successfully.");

    return "redirect:/admin/postedjobs";
}
}
