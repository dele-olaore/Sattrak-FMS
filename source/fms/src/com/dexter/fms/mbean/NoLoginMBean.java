package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Date;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.model.UploadedFile;

import com.dexter.common.util.Emailer;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.app.WorkOrderVendor;
import com.dexter.fms.util.SMSGateway;

@ManagedBean(name = "noLoginBean")
@SessionScoped
public class NoLoginMBean implements Serializable {
	private static final long serialVersionUID = 1L;

	final Logger logger = Logger.getLogger("fms-NoLoginMBean");
	
	private FacesMessage msg = null;
	
	private WorkOrderVendor wv;
	private UploadedFile workordFile;
	private String bidMessage = "";
	
	public NoLoginMBean() {
		
	}
	
	public void submitBid() {
		if(wv != null && getWv().getSubmittionStatus() != null) {
			if(getWv().getSubmittionStatus().equalsIgnoreCase("SUBMITTED")) {
				if(wv.getCost() != null && wv.getDays_of_completion() > 0 && workordFile != null) {
					wv.setSubmittionStatus("SUBMITTED");
					try {
						wv.setVendorDocument(workordFile.getContents()); 
					} catch(Exception ex) {
						ex.printStackTrace();
						msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Failed to upload the document!");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						return;
					}
					wv.setVendorResponse_dt(new Date());
					
					GeneralDAO gDAO = new GeneralDAO();
					gDAO.startTransaction();
					gDAO.update(wv);
					gDAO.commit();
					gDAO.destroy();
					
					StringBuilder sb = new StringBuilder("<html><body>");
					sb.append("<p>Hello,</p>");
					sb.append("<p>Vendor ").append(wv.getVendor().getName()).append(" just submitted a bid for work-order ").append(wv.getWorkOrder().getWorkOrderNumber()).append(".</p>");
					sb.append("<p>Regards<br/>Sattrak FMS</p>");
					sb.append("</body></html>");
					
					try {
						Emailer.sendEmail("fms@sattrakservices.com", new String[]{wv.getWorkOrder().getCreatedBy().getPersonel().getEmail()}, "Bid Submit for Work-order " + wv.getWorkOrder().getWorkOrderNumber(), sb.toString());
					} catch(Exception ex) {
						ex.printStackTrace();
					}
					try {
						SMSGateway.sendSMS("FMS", wv.getWorkOrder().getCreatedBy().getPersonel().getPhone(), "Vendor " + wv.getVendor().getName() + " just submitted a bid for work-order " + wv.getWorkOrder().getWorkOrderNumber());
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				} else {
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Please fill all fields!");
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			} else if(getWv().getSubmittionStatus().equalsIgnoreCase("DECLINED")) {
				wv.setSubmittionStatus("DECLINED");
				wv.setVendorResponse_dt(new Date());
				
				GeneralDAO gDAO = new GeneralDAO();
				gDAO.startTransaction();
				gDAO.update(wv);
				gDAO.commit();
				gDAO.destroy();
				
				StringBuilder sb = new StringBuilder("<html><body>");
				sb.append("<p>Hello,</p>");
				sb.append("<p>Vendor ").append(wv.getVendor().getName()).append(" just declined to submit a bid for work-order ").append(wv.getWorkOrder().getWorkOrderNumber()).append(".</p>");
				sb.append("<p>Regards<br/>Sattrak FMS</p>");
				sb.append("</body></html>");
				
				try {
					Emailer.sendEmail("fms@sattrakservices.com", new String[]{wv.getWorkOrder().getCreatedBy().getPersonel().getEmail()}, "Bid Submit Declined for Work-order " + wv.getWorkOrder().getWorkOrderNumber(), sb.toString());
				} catch(Exception ex) {
					ex.printStackTrace();
				}
				try {
					SMSGateway.sendSMS("FMS", wv.getWorkOrder().getCreatedBy().getPersonel().getPhone(), "Vendor " + wv.getVendor().getName() + " just declined to submit a bid for work-order " + wv.getWorkOrder().getWorkOrderNumber());
				} catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		} else {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Please fill all fields!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public boolean retriveWorkOrderVendor(long id) {
		boolean ret = false;
		GeneralDAO gDAO = new GeneralDAO();
		Object obj = gDAO.find(WorkOrderVendor.class, id);
		if(obj != null) {
			wv = (WorkOrderVendor)obj;
			if(wv.getWorkOrder().getMaxBidSubmission_dt().after(new Date()))
				ret = true;
			else {
				bidMessage = "Bidding for this work order is closed.";
			}
		}
		gDAO.destroy();
		
		return ret;
	}

	public WorkOrderVendor getWv() {
		return wv;
	}

	public void setWv(WorkOrderVendor wv) {
		this.wv = wv;
	}

	public UploadedFile getWorkordFile() {
		return workordFile;
	}

	public void setWorkordFile(UploadedFile workordFile) {
		this.workordFile = workordFile;
	}

	public String getBidMessage() {
		return bidMessage;
	}

	public void setBidMessage(String bidMessage) {
		this.bidMessage = bidMessage;
	}
}
