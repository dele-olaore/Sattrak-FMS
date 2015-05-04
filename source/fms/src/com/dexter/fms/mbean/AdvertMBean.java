package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.Query;

import org.primefaces.model.UploadedFile;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Advert;

@ManagedBean(name = "advertBean")
@SessionScoped
public class AdvertMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	private FacesMessage msg = null;
	
	private UploadedFile advertAnim;
	private Advert advert, topAdvert;
	private Vector<Advert> adverts;
	
	private String page = null;
	private Random random = new Random();
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public void createAdvert()
	{
		if(getAdvert().getType() != null && getAdvert().getCustomerName() != null &&
				getAdvert().getExpiryDate() != null && getAdvert().getProbability() > 0 &&
				getAdvertAnim() != null)
		{
			if(getAdvertAnim() != null)
				getAdvert().setContent(getAdvertAnim().getContents());
			getAdvert().setActive(true);
			getAdvert().setCreatedBy(dashBean.getUser());
			getAdvert().setCrt_dt(new Date());
			
			GeneralDAO gDAO = new GeneralDAO();
			
			gDAO.startTransaction();
			if(gDAO.save(getAdvert()))
			{
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Advert created successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setAdvert(null);
				setAdvertAnim(null);
				setAdverts(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Save failed. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			gDAO.destroy();
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}

	public UploadedFile getAdvertAnim() {
		return advertAnim;
	}

	public void setAdvertAnim(UploadedFile advertAnim) {
		this.advertAnim = advertAnim;
	}

	public Advert getAdvert() {
		if(advert == null)
			advert = new Advert();
		return advert;
	}

	public void setAdvert(Advert advert) {
		this.advert = advert;
	}
	
	public Advert getTopAdvert()
	{
		boolean research = false;
		if(page == null || topAdvert == null)
			research = true;
		else if(page != null)
		{
			if(!page.equalsIgnoreCase(dashBean.getFunction_page()))
			{
				research = true;
			}
		}
		
		page = dashBean.getFunction_page();
		
		if(research)
			topAdvert = getAdvertByType("TOP");
		
		return topAdvert;
	}
	
	public Advert getAdvertByType(String type)
	{
		Advert ad = null;
		if(getAdverts() != null)
		{
			if(getAdverts().size() > 0)
			{
				Vector<Long> advertsId = new Vector<Long>();
				for(Advert e : getAdverts())
				{
					if(e.getType().equalsIgnoreCase(type))
					{
						for(int i=0; i<e.getProbability(); i++)
						{
							advertsId.add(e.getId());
						}
					}
				}
				
				int choosen = random.nextInt(advertsId.size());
				Long choosenId = advertsId.get(choosen);
				for(Advert e : getAdverts())
				{
					if(e.getId().longValue() == choosenId.longValue())
					{
						ad = e;
						break;
					}
				}
			}
		}
		return ad;
	}

	public void resetAdverts()
	{
		setAdverts(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Advert> getAdverts() {
		if(adverts == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Query q = gDAO.createQuery("Select e from Advert e where e.active=:active");
			q.setParameter("active", true);
			
			Object cuss = gDAO.search(q, 0);
			if(cuss != null)
			{
				adverts = (Vector<Advert>)cuss;
			}
		}
		return adverts;
	}

	public void setAdverts(Vector<Advert> adverts) {
		this.adverts = adverts;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
