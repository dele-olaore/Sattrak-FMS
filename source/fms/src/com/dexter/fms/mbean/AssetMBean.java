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
import javax.persistence.Query;

import org.primefaces.event.RowEditEvent;
import org.primefaces.model.UploadedFile;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Item;
import com.dexter.fms.model.app.ItemSupply;
import com.dexter.fms.model.app.ItemUse;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.ref.ItemType;
import com.dexter.fms.model.ref.ServiceType;
import com.dexter.fms.model.ref.Vendor;
import com.dexter.fms.model.ref.VendorServices;

@ManagedBean(name = "assetBean")
@SessionScoped
public class AssetMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-AssetMBean");
	
	private FacesMessage msg = null;
	
	private UploadedFile itemPhoto;
	private Long itemType_id;
	private ItemType itemType;
	private Vector<ItemType> itemTypes;
	private Long item_id, vendor_id, vehicle_id;
	private Item item;
	private Vector<Item> items;
	private ItemUse itemUse;
	private ItemSupply itemSupply;

	private Vector<Vendor> vendors;
	private Vector<Fleet> fleets;
	private Long fleet_id;
	private String regNo;
	
	private Long partner_id;
	private Partner partner;
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public AssetMBean()
	{}
	
	public void onEdit(RowEditEvent event)
	{
		GeneralDAO gDAO = new GeneralDAO();
		boolean ret = false;
		Object eventSource = event.getObject();
		
		if(eventSource instanceof Item)
		{
			Item itm = (Item)eventSource;
			itm.setLastUpdatedDate(new Date());
			eventSource = itm;
		}
		
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
	
	public void saveItemSupply()
	{
		if(getItemSupply().getQuantity() > 0 && getItemSupply().getSupplyDate() != null 
				&& getItem_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object itmObj = gDAO.find(Item.class, getItem_id());
			if(itmObj != null)
				getItemSupply().setItem((Item)itmObj);
			
			if(getVendor_id() != null)
			{
				Object venObj = gDAO.find(Vendor.class, getVendor_id());
				if(venObj != null)
					getItemSupply().setVendor((Vendor)venObj);
			}
			
			long stockLevel = getItemSupply().getItem().getStockLevel();
			
			getItemSupply().setCreatedBy(dashBean.getUser());
			getItemSupply().setCrt_dt(new Date());
			
			gDAO.startTransaction();
			if(gDAO.save(getItemSupply()))
			{
				getItemSupply().getItem().setStockLevel(stockLevel + getItemSupply().getQuantity());
				gDAO.update(getItemSupply().getItem());
				
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Item supply saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setItemSupply(null);
				setItem_id(null);
				setVendor_id(null);
				setItems(null);
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
	
	public void saveItemUse()
	{
		if(getItem_id() != null && getItemUse().getQuantity() > 0 && getItemUse().getUseDate() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object itmObj = gDAO.find(Item.class, getItem_id());
			if(itmObj != null)
				getItemUse().setItem((Item)itmObj);
			
			if(getVehicle_id() != null)
			{
				Object vehObj = gDAO.find(Vehicle.class, getVehicle_id());
				if(vehObj != null)
					getItemUse().setVehicle((Vehicle)vehObj);
			}
			
			long stockLevel = getItemUse().getItem().getStockLevel();
			
			getItemUse().setCreatedBy(dashBean.getUser());
			getItemUse().setCrt_dt(new Date());
			
			gDAO.startTransaction();
			if(gDAO.save(getItemUse()))
			{
				getItemUse().getItem().setStockLevel(stockLevel - getItemUse().getQuantity());
				gDAO.update(getItemUse().getItem());
				
				gDAO.commit();
				
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Item usage saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setItemUse(null);
				setItem_id(null);
				setVehicle_id(null);
				setItems(null);
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
	
	public void saveItem()
	{
		if(getItem().getName() != null && getItemType_id() != null)
		{
			if(getItemPhoto() != null)
				getItem().setPhoto(getItemPhoto().getContents());
			getItem().setCreatedBy(dashBean.getUser());
			getItem().setCrt_dt(new Date());
			getItem().setPartner(getPartner());
			getItem().setLastUpdatedDate(new Date());
			
			GeneralDAO gDAO = new GeneralDAO();
			Object itype = gDAO.find(ItemType.class, getItemType_id());
			if(itype != null)
				getItem().setType((ItemType)itype);
			
			gDAO.startTransaction();
			if(gDAO.save(getItem()))
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Item saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setItem(null);
				setItems(null);
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
	
	public void saveItemType()
	{
		if(getItemType().getName() != null)
		{
			getItemType().setPartner(getPartner());
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			if(gDAO.save(getItemType()))
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Item type saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setItemType(null);
				setItemTypes(null);
			}
			else
			{
				gDAO.rollback();
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Save failed. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void resetAssets()
	{
		setItemTypes(null);
		setItemType(null);
		setItems(null);
		setItem(null);
		setItemUse(null);
		setItemSupply(null);
	}

	public UploadedFile getItemPhoto() {
		return itemPhoto;
	}

	public void setItemPhoto(UploadedFile itemPhoto) {
		this.itemPhoto = itemPhoto;
	}

	public Long getItemType_id() {
		return itemType_id;
	}

	public void setItemType_id(Long itemType_id) {
		this.itemType_id = itemType_id;
	}

	public ItemType getItemType() {
		if(itemType == null)
			itemType = new ItemType();
		return itemType;
	}

	public void setItemType(ItemType itemType) {
		this.itemType = itemType;
	}

	@SuppressWarnings("unchecked")
	public Vector<ItemType> getItemTypes() {
		boolean research = true;
		if(itemTypes == null || itemTypes.size() == 0)
			research = true;
		else if(itemTypes.size() > 0)
		{
			if(getPartner() != null)
			{
				if(itemTypes.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			itemTypes = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from ItemType e where e.partner=:partner");
				q.setParameter("partner", getPartner());
				
				Object cuss = gDAO.search(q, 0);
				if(cuss != null)
				{
					itemTypes = (Vector<ItemType>)cuss;
				}
			}
		}
		return itemTypes;
	}

	public void setItemTypes(Vector<ItemType> itemTypes) {
		this.itemTypes = itemTypes;
	}

	public Long getItem_id() {
		return item_id;
	}

	public void setItem_id(Long item_id) {
		this.item_id = item_id;
	}

	public Long getVendor_id() {
		return vendor_id;
	}

	public void setVendor_id(Long vendor_id) {
		this.vendor_id = vendor_id;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	public Item getItem() {
		if(item == null)
			item = new Item();
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	@SuppressWarnings("unchecked")
	public Vector<Item> getItems() {
		boolean research = true;
		if(items == null || items.size() == 0)
			research = true;
		else if(items.size() > 0)
		{
			if(getPartner() != null)
			{
				if(items.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			items = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from Item e where e.partner=:partner");
				q.setParameter("partner", getPartner());
				
				Object cuss = gDAO.search(q, 0);
				if(cuss != null)
				{
					items = (Vector<Item>)cuss;
				}
			}
		}
		return items;
	}

	public void setItems(Vector<Item> items) {
		this.items = items;
	}

	public ItemUse getItemUse() {
		if(itemUse == null)
			itemUse = new ItemUse();
		return itemUse;
	}

	public void setItemUse(ItemUse itemUse) {
		this.itemUse = itemUse;
	}

	public ItemSupply getItemSupply() {
		if(itemSupply == null)
			itemSupply = new ItemSupply();
		return itemSupply;
	}

	public void setItemSupply(ItemSupply itemSupply) {
		this.itemSupply = itemSupply;
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
				
				Object stypes = gDAO.findAll("ServiceType");
				if(stypes != null)
				{
					Vector<ServiceType> stypesList = (Vector<ServiceType>)stypes;
					ServiceType st = null;
					for(ServiceType e : stypesList)
					{
						if(e.getName().trim().equalsIgnoreCase("Vehicle Parts Sales"))
						{
							st = e;
							break;
						}
					}
					if(st != null)
					{
						Hashtable<String, Object> params = new Hashtable<String, Object>();
						params.put("vendor.partner", getPartner());
						params.put("serviceType", st);
						Object dpsObj = gDAO.search("VendorServices", params);
						if(dpsObj != null)
						{
							Vector<VendorServices> vss = (Vector<VendorServices>)dpsObj;
							vendors = new Vector<Vendor>();
							for(VendorServices vs : vss)
							{
								vendors.add(vs.getVendor());
							}
						}
					}
				}
			}
		}
		return vendors;
	}

	public void setVendors(Vector<Vendor> vendors) {
		this.vendors = vendors;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Fleet> getFleets() {
		boolean research = true;
		if(fleets == null || fleets.size() == 0)
			research = true;
		else if(fleets.size() > 0)
		{
			if(getPartner() != null)
			{
				if(fleets.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			fleets = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				Hashtable<String, Object> params = new Hashtable<String, Object>();
				params.put("partner", getPartner());
				Object dpsObj = gDAO.search("Fleet", params);
				if(dpsObj != null)
				{
					fleets = (Vector<Fleet>)dpsObj;
				}
			}
		}
		return fleets;
	}

	public void setFleets(Vector<Fleet> fleets) {
		this.fleets = fleets;
	}

	public Long getFleet_id() {
		return fleet_id;
	}

	public void setFleet_id(Long fleet_id) {
		this.fleet_id = fleet_id;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
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
	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
