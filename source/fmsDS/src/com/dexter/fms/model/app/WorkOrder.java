package com.dexter.fms.model.app;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.dexter.fms.model.Partner;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.ref.Vendor;

@Entity
public class WorkOrder implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue
	private Long id;
	@Column(unique=true)
	private String workOrderNumber;
	private String summaryDetailsOfWorkOrder;
	private String workOrderType; // Routine or Adhoc
	private boolean finished;
	private String status; // NEW, CANCELED, IN-PROGRESS, COMPLETED
	@ManyToOne
	private Vendor vendor;
	private byte[] vendorDocument;
	
	@ManyToOne
	private VehicleAdHocMaintenanceRequest adhocMainRequest; // Only available if picked and only for 'Adhoc' type
	
	@ManyToOne
	private Partner partner;
	@Temporal(TemporalType.TIMESTAMP)
	private Date crt_dt;
	@ManyToOne
	private PartnerUser createdBy;
	
	@Transient
	private Vector<WorkOrderVehicle> vehicles;
	@Transient
	private WorkOrderVehicle selectedVehicle;
	
	public WorkOrder()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWorkOrderNumber() {
		return workOrderNumber;
	}

	public void setWorkOrderNumber(String workOrderNumber) {
		this.workOrderNumber = workOrderNumber;
	}

	public String getSummaryDetailsOfWorkOrder() {
		return summaryDetailsOfWorkOrder;
	}

	public void setSummaryDetailsOfWorkOrder(String summaryDetailsOfWorkOrder) {
		this.summaryDetailsOfWorkOrder = summaryDetailsOfWorkOrder;
	}

	public String getWorkOrderType() {
		return workOrderType;
	}

	public void setWorkOrderType(String workOrderType) {
		this.workOrderType = workOrderType;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public byte[] getVendorDocument() {
		return vendorDocument;
	}

	public void setVendorDocument(byte[] vendorDocument) {
		this.vendorDocument = vendorDocument;
	}

	public VehicleAdHocMaintenanceRequest getAdhocMainRequest() {
		return adhocMainRequest;
	}

	public void setAdhocMainRequest(VehicleAdHocMaintenanceRequest adhocMainRequest) {
		this.adhocMainRequest = adhocMainRequest;
	}

	public Partner getPartner() {
		return partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}

	public PartnerUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(PartnerUser createdBy) {
		this.createdBy = createdBy;
	}

	public Vector<WorkOrderVehicle> getVehicles() {
		if(vehicles == null)
			vehicles = new Vector<WorkOrderVehicle>();
		return vehicles;
	}

	public void setVehicles(Vector<WorkOrderVehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public WorkOrderVehicle getSelectedVehicle() {
		return selectedVehicle;
	}

	public void setSelectedVehicle(WorkOrderVehicle selectedVehicle) {
		this.selectedVehicle = selectedVehicle;
	}
	
}
