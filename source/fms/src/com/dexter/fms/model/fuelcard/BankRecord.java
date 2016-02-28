package com.dexter.fms.model.fuelcard;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.dexter.fms.model.app.Vehicle;

@Entity
@Table(name="BANKRECORD_TB")
public class BankRecord implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
    @NotNull
    @GeneratedValue
	private Long id;
	
	private String cusName;
	private String cusPhone;
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date tranTime;
	private String tranTimeStr;
	private String tranType;
	private double tranAmt;
	private double tranFees;
	private String tranStatus;
	private String cardPan;
	private String cardStatus;
	private String schemeOwner;
	private String cardAcceptorId;
	private String cardAcceptorLoc;
	private String retrievalRefNum;
	private double cardBal;
	
	private double tranID;
	
	@ManyToOne
	private Vehicle vehicle;
	
	private boolean alerted;
	
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date crt_dt;
	
	public BankRecord()
	{}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCusName() {
		return cusName;
	}

	public void setCusName(String cusName) {
		this.cusName = cusName;
	}

	public String getCusPhone() {
		return cusPhone;
	}

	public void setCusPhone(String cusPhone) {
		this.cusPhone = cusPhone;
	}

	public Date getTranTime() {
		return tranTime;
	}

	public void setTranTime(Date tranTime) {
		this.tranTime = tranTime;
	}

	public String getTranTimeStr() {
		return tranTimeStr;
	}

	public void setTranTimeStr(String tranTimeStr) {
		this.tranTimeStr = tranTimeStr;
	}

	public String getTranType() {
		return tranType;
	}

	public void setTranType(String tranType) {
		this.tranType = tranType;
	}

	public double getTranAmt() {
		return tranAmt;
	}

	public void setTranAmt(double tranAmt) {
		this.tranAmt = tranAmt;
	}

	public double getTranFees() {
		return tranFees;
	}

	public void setTranFees(double tranFees) {
		this.tranFees = tranFees;
	}

	public String getTranStatus() {
		return tranStatus;
	}

	public void setTranStatus(String tranStatus) {
		this.tranStatus = tranStatus;
	}

	public String getCardPan() {
		return cardPan;
	}

	public void setCardPan(String cardPan) {
		this.cardPan = cardPan;
	}

	public String getCardStatus() {
		return cardStatus;
	}

	public void setCardStatus(String cardStatus) {
		this.cardStatus = cardStatus;
	}

	public String getSchemeOwner() {
		return schemeOwner;
	}

	public void setSchemeOwner(String schemeOwner) {
		this.schemeOwner = schemeOwner;
	}

	public String getCardAcceptorId() {
		return cardAcceptorId;
	}

	public void setCardAcceptorId(String cardAcceptorId) {
		this.cardAcceptorId = cardAcceptorId;
	}

	public String getCardAcceptorLoc() {
		return cardAcceptorLoc;
	}

	public void setCardAcceptorLoc(String cardAcceptorLoc) {
		this.cardAcceptorLoc = cardAcceptorLoc;
	}

	public String getRetrievalRefNum() {
		return retrievalRefNum;
	}

	public void setRetrievalRefNum(String retrievalRefNum) {
		this.retrievalRefNum = retrievalRefNum;
	}

	public double getCardBal() {
		return cardBal;
	}

	public void setCardBal(double cardBal) {
		this.cardBal = cardBal;
	}

	public double getTranID() {
		return tranID;
	}

	public void setTranID(double tranID) {
		this.tranID = tranID;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public boolean isAlerted() {
		return alerted;
	}

	public void setAlerted(boolean alerted) {
		this.alerted = alerted;
	}

	public Date getCrt_dt() {
		return crt_dt;
	}

	public void setCrt_dt(Date crt_dt) {
		this.crt_dt = crt_dt;
	}
	
}
