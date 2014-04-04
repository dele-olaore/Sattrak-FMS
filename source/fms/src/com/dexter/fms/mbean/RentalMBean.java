package com.dexter.fms.mbean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Calendar;
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
import javax.servlet.ServletContext;

import org.primefaces.event.RowEditEvent;

import com.dexter.common.util.Emailer;
import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.app.Fleet;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.app.VehicleDriver;
import com.dexter.fms.model.app.rentals.Customer;
import com.dexter.fms.model.app.rentals.CustomerBooking;
import com.dexter.fms.model.app.rentals.PriceSetting;
import com.dexter.fms.model.ref.RentalServiceType;
import com.dexter.fms.model.ref.VehicleType;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;

@ManagedBean(name = "rentalBean")
@SessionScoped
public class RentalMBean implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	final Logger logger = Logger.getLogger("fms-RentalMBean");
	
	private FacesMessage msg = null;
	
	private Customer customer;
	private Vector<Customer> customers;
	
	private RentalServiceType serviceType;
	private Vector<RentalServiceType> serviceTypes;
	private Long serviceType_id, serviceType_id2;
	private PriceSetting priceSetting;
	private Vector<PriceSetting> priceSettings;
	
	private Long customer_id;
	private CustomerBooking booking;
	private Vector<CustomerBooking> bookings, paidBookings;
	
	private Long partner_id, vehicleType_id, vehicleType_id2, vehicle_id;
	private Vector<VehicleType> vehicleTypes;
	
	private Vector<Fleet> fleets;
	private Long fleet_id;
	private String regNo;
	
	private Partner partner;
	@ManagedProperty("#{dashboardBean}")
	DashboardMBean dashBean;
	
	public RentalMBean()
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
	
	public void completeTrip()
	{
		if(getBooking() != null && getBooking().getId() != null
				&& getBooking().isPaid())
		{
			getBooking().setStatus("COMPLETED");
			
			GeneralDAO gDAO = new GeneralDAO();
			
			gDAO.startTransaction();
			if(gDAO.update(getBooking()))
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Trip completed successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setBooking(null);
				setPaidBookings(null);
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
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Invalid trip selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void payForBooking()
	{
		if(getBooking().getId() != null && getVehicle_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			getBooking().setPaid(true);
			if(getBooking().getBook_dt().before(new Date()))
				getBooking().setStatus("ONGOING");
			else
				getBooking().setStatus("AWAITING");
			Object v = gDAO.find(Vehicle.class, getVehicle_id());
			if(v != null)
				getBooking().setVehicle((Vehicle)v);
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicle", getBooking().getVehicle());
			params.put("active", true);
			
			Object vdrs = gDAO.search("VehicleDriver", params);
			if(vdrs != null)
			{
				Vector<VehicleDriver> vdrsList = (Vector<VehicleDriver>)vdrs;
				for(VehicleDriver vd : vdrsList)
					getBooking().setDriver(vd.getDriver());
			}
			
			boolean ret = false;
			gDAO.startTransaction();
			ret = gDAO.update(getBooking());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Booking payment saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setBooking(null);
				setBookings(null);
				setPaidBookings(null);
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
	
	public void cancelBooking()
	{
		if(getBooking() != null && getBooking().getId() != null)
		{
			getBooking().setStatus("CANCELED");
			
			GeneralDAO gDAO = new GeneralDAO();
			
			gDAO.startTransaction();
			if(gDAO.update(getBooking()))
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Booking request canceled successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setBooking(null);
				setBookings(null);
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
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Invalid booking request selected!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void startBooking()
	{
		if(getBooking().getBook_dt() != null && getBooking().getPickupLocation() != null && getBooking().getDestination() != null &&
				getCustomer_id() != null && getBooking().getDurationCount() > 0 && getBooking().getDurationType() != null && 
				getVehicleType_id2() != null && getServiceType_id2() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object cus = gDAO.find(Customer.class, getCustomer_id());
			if(cus != null)
				getBooking().setCustomer((Customer)cus);
			Object vtype = gDAO.find(VehicleType.class, getVehicleType_id2());
			if(vtype != null)
				getBooking().setVehicleType((VehicleType)vtype);
			
			Object svtype = gDAO.find(RentalServiceType.class, getServiceType_id2());
			if(svtype != null)
				getBooking().setServiceType((RentalServiceType)svtype);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(getBooking().getBook_dt());
			
			if(getBooking().getDurationType().equalsIgnoreCase("HOUR"))
				cal.add(Calendar.HOUR_OF_DAY, getBooking().getDurationCount());
			else if(getBooking().getDurationType().equalsIgnoreCase("DAY"))
				cal.add(Calendar.DATE, getBooking().getDurationCount());
			else if(getBooking().getDurationType().equalsIgnoreCase("WEEK"))
				cal.add(Calendar.DATE, getBooking().getDurationCount()*7);
			else if(getBooking().getDurationType().equalsIgnoreCase("MONTH"))
				cal.add(Calendar.MONTH, getBooking().getDurationCount());
			else if(getBooking().getDurationType().equalsIgnoreCase("YEAR"))
				cal.add(Calendar.YEAR, getBooking().getDurationCount());
			
			getBooking().setEnd_dt(cal.getTime());
			
			getBooking().setStatus("PENDING_PAYMENT");
			
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("vehicleType", getBooking().getVehicleType());
			params.put("serviceType", getBooking().getServiceType());
			params.put("durationType", getBooking().getDurationType());
			params.put("partner", getBooking().getCustomer().getPartner());
			
			Object priceSettObj = gDAO.search("PriceSetting", params);
			if(priceSettObj != null)
			{
				Vector<PriceSetting> priceSettList = (Vector<PriceSetting>)priceSettObj;
				if(priceSettList.size() > 0)
				{
					PriceSetting sett = null;
					for(PriceSetting e : priceSettList)
						sett = e;
					
					getBooking().setAmount(sett.getAmountPerDurationType()*getBooking().getDurationCount());
					
					boolean ret = false;
					gDAO.startTransaction();
					if(getBooking().getId() == null)
					{
						getBooking().setCreatedBy(dashBean.getUser());
						getBooking().setCrt_dt(new Date());
						ret = gDAO.save(getBooking());
					}
					else
					{
						ret = gDAO.update(getBooking());
					}
					if(ret)
					{
						Document document = new Document();
						try
						{
							ByteArrayOutputStream byout = new ByteArrayOutputStream();
							PdfWriter.getInstance(document, byout);
							
							document.open();
							document.setPageSize(PageSize.A4);
							document.addAuthor("FMS");
							document.addCreationDate();
							document.addCreator("FMS");
							document.addSubject("Invoice");
							document.addTitle("Invoice for trip booking");
							
							PdfPTable headerTable = new PdfPTable(1);
							
							ServletContext servletContext = (ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext();
							String logo = servletContext.getRealPath("") + File.separator + "resources" + File.separator + "images" + File.separator + "satraklogo.jpg";
							
							params = new Hashtable<String, Object>();
							params.put("partner", dashBean.getUser().getPartner());
							Object pSettingsObj = gDAO.search("PartnerSetting", params);
							PartnerSetting setting = null;
							if(pSettingsObj != null)
							{
								Vector<PartnerSetting> pSettingsList = (Vector<PartnerSetting>)pSettingsObj;
								for(PartnerSetting e : pSettingsList)
								{
									setting = e;
								}
							}
							
							PdfPCell c = null;
							if(setting != null && setting.getLogo() != null)
							{
								Image logoImg = Image.getInstance(setting.getLogo());
								logoImg.scaleToFit(212, 51);
								c = new PdfPCell(logoImg);
							}
							else
								c = new PdfPCell(Image.getInstance(logo));
							c.setBorder(0);
							c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
							headerTable.addCell(c);
							
							c = new PdfPCell(new Phrase("Invoice"));
							c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
							c.setBorder(0);
							headerTable.addCell(c);
							
							document.add(headerTable);
							
							document.add(new Paragraph("Dear " + getBooking().getCustomer().getFirstname()));
							
							PdfPTable bodyTable = new PdfPTable(5); // date, hours, vehicle type, service type, amount
							
							bodyTable.addCell("Date");bodyTable.addCell("Duration");
							bodyTable.addCell("Vehicle Type");bodyTable.addCell("Service Type");
							bodyTable.addCell("Amount");
							
							c = new PdfPCell(new Phrase(getBooking().getBook_dt().toLocaleString()));
							c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
							bodyTable.addCell(c);
							c = new PdfPCell(new Phrase(getBooking().getDurationCount() + " " + getBooking().getDurationType()));
							c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
							bodyTable.addCell(c);
							c = new PdfPCell(new Phrase(getBooking().getVehicleType().getName()));
							c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
							bodyTable.addCell(c);
							c = new PdfPCell(new Phrase(getBooking().getServiceType().getName()));
							c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
							bodyTable.addCell(c);
							c = new PdfPCell(new Phrase(""+getBooking().getAmount()));
							c.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
							bodyTable.addCell(c);
							
							document.add(bodyTable);
							
							document.close(); // no need to close PDFwriter?
							
							getBooking().setInvoice(byout.toByteArray());
							
							gDAO.update(getBooking());
							
							final byte[] data = byout.toByteArray();
							Thread thread = new Thread()
							{
								public void run()
								{
									Emailer.sendEmail("fms@sattrakservices.com", new String[] {getBooking().getCustomer().getEmail()}, "Booking Invoice", "<html><body><p>Dear " + getBooking().getCustomer().getFirstname() + ",</p><p>Please find attached to this email, your booking invoice.</p><p>Best Regards</p></body></html>", data, "booking_invoice.pdf");
								}
							};
							thread.start();
						}
						catch(DocumentException e) {
							e.printStackTrace();
						}
						catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
						catch (IOException e1) {
							e1.printStackTrace();
						}
						
						gDAO.commit();
						msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Booking request saved successfully.");
						FacesContext.getCurrentInstance().addMessage(null, msg);
						
						setBooking(null);
						setBookings(null); // bookings pending payment
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
					msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Search failed for price setting. " + gDAO.getMessage());
					FacesContext.getCurrentInstance().addMessage(null, msg);
				}
			}
			else
			{
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "Search failed for price setting. " + gDAO.getMessage());
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
		}
		else
		{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error: ", "All fields with '*' sign are required!");
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
	}
	
	public void saveRentalServiceType()
	{
		if(getServiceType().getName() != null)
		{
			getServiceType().setPartner(getPartner());
			getServiceType().setCreatedBy(dashBean.getUser());
			getServiceType().setCrt_dt(new Date());
			
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			if(getServiceType().getId() != null)
				ret = gDAO.update(getServiceType());
			else
				ret = gDAO.save(getServiceType());
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Service type saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setServiceType(null);
				setServiceTypes(null);
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
	
	public void saveSetting()
	{
		if(getPriceSetting().getAmountPerDurationType() > 0 && getServiceType_id() != null &&
				getVehicleType_id() != null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			Object vtype = gDAO.find(VehicleType.class, getVehicleType_id());
			if(vtype != null)
				getPriceSetting().setVehicleType((VehicleType)vtype);
			
			Object svtype = gDAO.find(RentalServiceType.class, getServiceType_id());
			if(svtype != null)
				getPriceSetting().setServiceType((RentalServiceType)svtype);
			
			getPriceSetting().setPartner(getPartner());
			boolean ret = false;
			gDAO.startTransaction();
			if(getPriceSetting().getId() == null)
			{
				getPriceSetting().setCreatedBy(dashBean.getUser());
				getPriceSetting().setCrt_dt(new Date());
				ret = gDAO.save(getPriceSetting());
			}
			else
			{
				ret = gDAO.update(getPriceSetting());
			}
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Price setting saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setPriceSetting(null);
				setPriceSettings(null);
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
	
	public void saveCustomer()
	{
		if(getCustomer().getFirstname() != null)
		{
			getCustomer().setPartner(getPartner());
			GeneralDAO gDAO = new GeneralDAO();
			gDAO.startTransaction();
			boolean ret = false;
			if(getCustomer().getId() == null)
			{
				getCustomer().setCreatedBy(dashBean.getUser());
				getCustomer().setCrt_dt(new Date());
				ret = gDAO.save(getCustomer());
			}
			else
			{
				ret = gDAO.update(getCustomer());
			}
			if(ret)
			{
				gDAO.commit();
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Success: ", "Customer saved successfully.");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				
				setCustomer(null);
				setCustomers(null);
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
	
	public void resetBookings()
	{
		setPriceSettings(null);
		setPriceSetting(null);
		setBookings(null);
		setBooking(null);
		setPaidBookings(null);
	}
	
	public void resetCustomers()
	{
		setCustomers(null);
		setCustomer(null);
	}

	public Customer getCustomer() {
		if(customer == null)
			customer = new Customer();
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@SuppressWarnings("unchecked")
	public Vector<Customer> getCustomers() {
		boolean research = true;
		if(customers == null || customers.size() == 0)
			research = true;
		else if(customers.size() > 0)
		{
			if(getPartner() != null)
			{
				if(customers.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			customers = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from Customer e where e.partner=:partner");
				q.setParameter("partner", getPartner());
				
				Object cuss = gDAO.search(q, 0);
				if(cuss != null)
				{
					customers = (Vector<Customer>)cuss;
				}
			}
		}
		return customers;
	}

	public void setCustomers(Vector<Customer> customers) {
		this.customers = customers;
	}

	public RentalServiceType getServiceType() {
		if(serviceType == null)
			serviceType = new RentalServiceType();
		return serviceType;
	}

	public void setServiceType(RentalServiceType serviceType) {
		this.serviceType = serviceType;
	}

	@SuppressWarnings("unchecked")
	public Vector<RentalServiceType> getServiceTypes() {
		boolean research = true;
		if(serviceTypes == null || serviceTypes.size() == 0)
			research = true;
		else if(serviceTypes.size() > 0)
		{
			if(getPartner() != null)
			{
				if(serviceTypes.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			serviceTypes = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from RentalServiceType e where e.partner=:partner");
				q.setParameter("partner", getPartner());
				
				Object cuss = gDAO.search(q, 0);
				if(cuss != null)
				{
					serviceTypes = (Vector<RentalServiceType>)cuss;
				}
			}
		}
		return serviceTypes;
	}

	public void setServiceTypes(Vector<RentalServiceType> serviceTypes) {
		this.serviceTypes = serviceTypes;
	}

	public Long getServiceType_id() {
		return serviceType_id;
	}

	public void setServiceType_id(Long serviceType_id) {
		this.serviceType_id = serviceType_id;
	}

	public Long getServiceType_id2() {
		return serviceType_id2;
	}

	public void setServiceType_id2(Long serviceType_id2) {
		this.serviceType_id2 = serviceType_id2;
	}

	public PriceSetting getPriceSetting() {
		if(priceSetting == null)
			priceSetting = new PriceSetting();
		return priceSetting;
	}

	public void setPriceSetting(PriceSetting priceSetting) {
		this.priceSetting = priceSetting;
	}

	@SuppressWarnings("unchecked")
	public Vector<PriceSetting> getPriceSettings() {
		boolean research = true;
		if(priceSettings == null || priceSettings.size() == 0)
			research = true;
		else if(priceSettings.size() > 0)
		{
			if(getPartner() != null)
			{
				if(priceSettings.get(0).getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			priceSettings = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from PriceSetting e where e.partner=:partner");
				q.setParameter("partner", getPartner());
				
				Object setts = gDAO.search(q, 0);
				if(setts != null)
				{
					priceSettings = (Vector<PriceSetting>)setts;
				}
			}
		}
		return priceSettings;
	}

	public void setPriceSettings(Vector<PriceSetting> priceSettings) {
		this.priceSettings = priceSettings;
	}

	public Long getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(Long customer_id) {
		this.customer_id = customer_id;
	}

	public CustomerBooking getBooking() {
		if(booking == null)
			booking = new CustomerBooking();
		return booking;
	}

	public void setBooking(CustomerBooking booking) {
		this.booking = booking;
	}

	@SuppressWarnings("unchecked")
	public Vector<CustomerBooking> getBookings() {
		boolean research = true;
		if(bookings == null || bookings.size() == 0)
			research = true;
		else if(bookings.size() > 0)
		{
			if(getPartner() != null)
			{
				if(bookings.get(0).getCustomer().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			bookings = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from CustomerBooking e where e.customer.partner=:partner and e.status = :status");
				q.setParameter("partner", getPartner());
				q.setParameter("status", "PENDING_PAYMENT");
				
				Object books = gDAO.search(q, 0);
				if(books != null)
				{
					bookings = (Vector<CustomerBooking>)books;
				}
			}
		}
		return bookings;
	}

	public void setBookings(Vector<CustomerBooking> bookings) {
		this.bookings = bookings;
	}
	
	@SuppressWarnings("unchecked")
	public Vector<CustomerBooking> getPaidBookings() {
		boolean research = true;
		if(paidBookings == null || paidBookings.size() == 0)
			research = true;
		else if(paidBookings.size() > 0)
		{
			if(getPartner() != null)
			{
				if(paidBookings.get(0).getCustomer().getPartner().getId() == getPartner().getId())
					research = false;
			}
		}
		if(research)
		{
			paidBookings = null;
			if(getPartner() != null)
			{
				GeneralDAO gDAO = new GeneralDAO();
				
				Query q = gDAO.createQuery("Select e from CustomerBooking e where e.customer.partner=:partner and e.paid=:paid and (e.status = :status1 or e.status = :status2)");
				q.setParameter("partner", getPartner());
				q.setParameter("paid", true);
				q.setParameter("status1", "AWAITING");
				q.setParameter("status2", "ONGOING");
				
				Object books = gDAO.search(q, 0);
				if(books != null)
				{
					paidBookings = (Vector<CustomerBooking>)books;
				}
			}
		}
		return paidBookings;
	}

	public void setPaidBookings(Vector<CustomerBooking> paidBookings) {
		this.paidBookings = paidBookings;
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
	
	public Long getVehicleType_id() {
		return vehicleType_id;
	}

	public void setVehicleType_id(Long vehicleType_id) {
		this.vehicleType_id = vehicleType_id;
	}
	
	public Long getVehicleType_id2() {
		return vehicleType_id2;
	}

	public void setVehicleType_id2(Long vehicleType_id2) {
		this.vehicleType_id2 = vehicleType_id2;
	}

	public Long getVehicle_id() {
		return vehicle_id;
	}

	public void setVehicle_id(Long vehicle_id) {
		this.vehicle_id = vehicle_id;
	}

	@SuppressWarnings("unchecked")
	public Vector<VehicleType> getVehicleTypes() {
		if(vehicleTypes == null)
		{
			GeneralDAO gDAO = new GeneralDAO();
			
			Object dpsObj = gDAO.findAll("VehicleType");
			if(dpsObj != null)
			{
				vehicleTypes = (Vector<VehicleType>)dpsObj;
			}
		}
		return vehicleTypes;
	}

	public void setVehicleTypes(Vector<VehicleType> vehicleTypes) {
		this.vehicleTypes = vehicleTypes;
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

	public DashboardMBean getDashBean() {
		return dashBean;
	}

	public void setDashBean(DashboardMBean dashBean) {
		this.dashBean = dashBean;
	}
	
}
