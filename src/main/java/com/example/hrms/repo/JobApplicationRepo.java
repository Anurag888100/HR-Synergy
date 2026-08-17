package com.example.hrms.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hrms.model.JobApplication;

public interface JobApplicationRepo extends JpaRepository<JobApplication, Integer> {

    List<JobApplication> findByUserid(int userid);

    List<JobApplication> findByJobid(int jobid);

    long countByUserid(int userid);

    long countByUseridAndStatus(int userid, String status);

    boolean existsByUseridAndJobid(int userid, int jobid);
   

    // New Method
    long countByStatus(String status);
    
    @Query("""
    	       SELECT j.title, COUNT(a)
    	       FROM JobApplication a
    	       JOIN JobInfo j
    	       ON a.jobid = j.id
    	       GROUP BY j.title
    	       ORDER BY COUNT(a) DESC
    	       """)
    	List<Object[]> findMostAppliedJobs();

}