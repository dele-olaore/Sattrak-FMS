package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.RowEditEvent;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.ref.ServiceType;
import com.dexter.fms.model.ref.Vendor;
import com.dexter.fms.model.ref.VendorServices;

@ManagedBean(name = "vendorBean")
@SessionScoped
public class VendorMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-VendorMBean");
	
	private FacesMessage msg = null;
	
	private Long partner_id;
	private Partner partner;
	
	private Vector<ServiceType> serviceTypes;
	
	private Vendor vendor;
	private Vector<Vendor> vendors;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public VendorMBean()
	{}
	
	public void onEdit(RowEditEvent event)
	{
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false;
		Object eventSource = event.getObject();
		
		gDAO.startTransaction();
		ret = gDAO.update(eventSource);
		
		if(ret)
		{
			gDAO.commit();
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Entity updated successfully.");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
		{
			gDAO.rollback();
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to update entity. " + gDAO.getMessage());
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		gDAO.destroy();
	}
	
	public void onCancel(RowEditEvent event)
	{
		msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Update canceled!");
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}
	
	public void saveVendor()
	{
		if(getPartner() != null && getVendor().getName() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			getVendor().setCreatedBy(dashBean.getUser());
			getVendor().setCrt_dt(new Date());
			getVendor().setPartner(getPartner());
			
			gDAO.startTransaction();
			boolean ret = gDAO.save(getVendor());
			if(ret)
			{
				for(ServiceType st : getServiceTypes())
				{
					if(st.isSelected())
					{
						VendorServices vs = new VendorServices();
						vs.setCreatedBy(dashBean.getUser());
						vs.setCrt_dt(new Date());
						vs.setServiceType(st);
						vs.setVendor(getVendor());
						
						gDAO.save(vs);
					}
				}
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Vendor created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setVendor(null);
				setVendors(null);
				setServiceTypes(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "Failed to create vendor. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed: ", "All fields with the '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public Partner getPartner() {
		if(!dashBean.getUser().getPartner().isSattrak())
		{
			partner = dashBean.getUser().getPartner();
		}
		else
		{
			if(getPartner_id() != null)
			{
				partner = (Partner)new GeneralDAO().find(Partner.class, getPartner_id());
			}
		}
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}
	
	public Long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(Long partner_id) {
		this.partner_id = partner_id;
	}

	@SuppressWarnings("unchecked")
	public Vector<ServiceType> getServiceTypes() {
		if(serviceTypes == null)
		{
			Object objList = new GeneralDAO().findAll("ServiceType");
			if(objList != null)
				serviceTypes = (Vector<ServiceType>)objList;
		}
		return serviceTypes;
	}

	public void setServiceTypes(Vector<ServiceType> serviceTypes) {
		this.serviceTypes = serviceTypes;
	}

	public Vendor getVendor() {
		if(vendor == null)
			vendor = new Vendor();
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}
	
	public void resetVendors()
	{
		setVendors(null);
	}

	@SuppressWarnings("unchecked")
	public Vector<Vendor> getVendors() {
		boolean research = true;
		if(vendors == null || vendors.size() == 0)
			research = true;
		else if(vendors.size() > 0)
		{
			if(getPartner() != null)
			{
				if(vendors.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			vendors = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Vendor", params);
				if(dpsObj != null)
				{
					vendors = (Vector<Vendor>)dpsObj;
					for(Vendor e : vendors)
					{
						params = new Hashtable<String, Object>();
						params.put("vendor", e);
						Object objList = gDAO.search("VendorServices", params);
						if(objList != null)
							e.setServices((Vector<VendorServices>)objList);
					}
				}
			}
		}
		return vendors;
	}

	public void setVendors(Vector<Vendor> vendors) {
		this.vendors = vendors;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
