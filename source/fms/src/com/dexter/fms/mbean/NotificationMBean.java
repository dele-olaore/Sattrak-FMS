package com.dexter.fms.mbean;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

//import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Notification;

@ManagedBean(name = "notifyBean")
@SessionScoped
public class NotificationMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-FleetMBean");
	
	//private FacesMessage msg = null;
	
	private Calendar lastUpdated;
	private Vector<Notification> notifications;
	
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public NotificationMBean()
	{
		lastUpdated = Calendar.getInstance();
	}

	public void markAll()
	{
		if(getNotifications() != null && getNotifications().size() > 0)
		{
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			for(Notification e : getNotifications())
			{
				e.setNotified(true);
				gDAO.update(e);
			}
			gDAO.commit();
			gDAO.destroy();
			
			resetNotifications();
		}
	}
	
	public String gotoNotificationPage(Long notification_id, String page, boolean subFunction)
	{
		GeneralDAO gDAO = new GeneralDAO();
		Object notif = gDAO.find(Notification.class, notification_id);
		if(notif != null)
		{
			Notification n = (Notification) notif;
			n.setNotified(true);
			gDAO.startTransaction();
			if(gDAO.update(n))
				gDAO.commit();
			
			gDAO.destroy();
			
			resetNotifications();
		}
		
		if(!subFunction)
			dashBean.setFunction_page(page);
		return page;
	}
	
	public void resetNotifications()
	{
		setNotifications(null);
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Notification> getNotifications() {
		if(notifications == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("user", dashBean.getUser());
			params.put("notified", false);
			
			Object objs = gDAO.search("Notification", params);
			if(objs != null)
			{
				notifications = (Vector<Notification>)objs;
			}
			gDAO.destroy();
			
			lastUpdated = Calendar.getInstance();
		}
		else
		{
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MINUTE, -2);
			if(c.after(lastUpdated))
			{
				notifications = null;
				return getNotifications();
			}
		}
		return notifications;
	}

	public void setNotifications(Vector<Notification> notifications) {
		this.notifications = notifications;
	}

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
