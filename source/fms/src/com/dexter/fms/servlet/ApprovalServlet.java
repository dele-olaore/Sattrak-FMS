package com.dexter.fms.servlet;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dexter.common.util.Emailer;
import com.dexter.fms.model.app.Expense;
import com.dexter.fms.model.app.ExpenseRequest;

/**
 * Servlet implementation class ApprovalServlet
 */
@WebServlet(description = "Servlet to attend to requests via email", urlPatterns = { "/approvalservlet" })
public class ApprovalServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	private static final String PERSISTENCE_UNIT_NAME = "fms";
    private static EntityManagerFactory factory;
    private EntityManager em;
    
    final Logger logger = Logger.getLogger("fms-ApprovalServlet");
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ApprovalServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String html = "<html><body><h2>Action Response</h2><br/>";
			
			factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
			em = factory.createEntityManager();
			
			String expId = request.getParameter("expId");
			String usrId = request.getParameter("usrId");
			String apv = request.getParameter("apv");
			
			if(expId != null && usrId != null && apv != null)
			{
				ExpenseRequest expR = null;
				try
				{
					Object expRObj = em.find(ExpenseRequest.class, Long.parseLong(expId));
					if(expRObj != null)
						expR = (ExpenseRequest)expRObj;
				}
				catch(Exception ex){}
				if(expR != null)
				{
					if(expR.getApproval_dt() != null) {
						html += "<p><font color=red size=12>Error: You already attended to this request!</font></p>";
					} else {
						try
						{
							if(expR.getApprovalUser().getId().longValue() == Long.parseLong(usrId))
							{
								if(apv.equals("1"))
								{
									EntityTransaction utx = em.getTransaction();
									utx.begin();
									
									expR.setApprovalComment("Approved");
									expR.setApprovalStatus("APPROVED");
									expR.setApproval_dt(new Date());
									em.merge(expR);
									
									/*Expense exp = new Expense();
									exp.setAmount(expR.getAmount());
									exp.setCreatedBy(expR.getCreatedBy());
									exp.setCrt_dt(new Date());
									exp.setExpense_dt(expR.getExpense_dt());
									exp.setPartner(expR.getPartner());
									exp.setRemarks(expR.getRemarks());
									exp.setType(expR.getType());
									em.persist(exp);
									
									expR.setExpense(exp);
									em.merge(expR);*/
									
									utx.commit();
									
									String email_message = "<html><body><p><strong>Dear " + expR.getCreatedBy().getPersonel().getFirstname() + ", </strong></p>";
									email_message += "<p>Your expense request for " + expR.getType() + ": " + expR.getRemarks() + " has been approved! You can now make the expense and capture it on the platform.</p>";
									email_message += "<p>Regards<br/>FMS</p></body></html>";
									
									try {
										Emailer.sendEmail("fms@sattrakservices.com", new String[]{expR.getCreatedBy().getPersonel().getEmail()}, "Expense Approved", email_message);
									} catch(Exception ex){}
									
									html += "<p><font color=green size=12>Success: Expense approved successfully!</font></p>";
								}
								else if(apv.equals("0"))
								{
									EntityTransaction utx = em.getTransaction();
									utx.begin();
									
									expR.setApprovalComment("Denied");
									expR.setApprovalStatus("DENIED");
									expR.setApproval_dt(new Date());
									em.merge(expR);
									
									utx.commit();
									
									String email_message = "<html><body><p><strong>Dear " + expR.getCreatedBy().getPersonel().getFirstname() + ", </strong></p>";
									email_message += "<p>Your expense request for " + expR.getType() + ": " + expR.getRemarks() + " has been <font color=red>denied!</font></p>";
									email_message += "<p>Regards<br/>FMS</p></body></html>";
									
									try {
										Emailer.sendEmail("fms@sattrakservices.com", new String[]{expR.getCreatedBy().getPersonel().getEmail()}, "Expense Denied", email_message);
									} catch(Exception ex){}
									
									html += "<p><font color=green size=12>Success: Expense denied successfully!</font></p>";
								}
								else
								{
									html += "<p><font color=red size=12>Error: Invalid parameter detected!</font></p>";
								}
							}
							else
							{
								html += "<p><font color=red size=12>Failed: Request can only be approved by the approval personel!</font></p>";
							}
						}
						catch(Exception ex)
						{
							html += "<p><font color=red size=12>Error: Invalid parameter detected!</font></p>";
						}
					}
				}
				else
				{
					html += "<p><font color=red size=12>Failed: Unknown expense request!</font></p>";
				}
			}
			else
			{
				html += "<p><font color=red size=12>Error: Request values not found!</font></p>";
			}
			
			html += "</body></html>";
			
			//em.flush();
			try {
				em.close();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
			BufferedOutputStream output = null;
			
	        try {
	        	output = new BufferedOutputStream(response.getOutputStream());
	        	output.write(html.getBytes());
	        } finally {
	            if (output != null) try { output.close(); } catch (IOException logOrIgnore) {}
	        }
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
