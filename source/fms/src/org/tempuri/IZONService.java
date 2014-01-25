/**
 * IZONService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.tempuri;

public interface IZONService extends java.rmi.Remote {
    public java.lang.String galooli() throws java.rmi.RemoteException;
    public java.lang.String test(java.lang.String text) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsResult getUnits(java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsDetailsResult getUnitsInfo() throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEvent[] zGetDailyEvents(java.util.Calendar date) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SATTRACKUnitEventResult SATTRAK_GetDailyEvents(java.util.Calendar startdate, java.util.Calendar enddate, java.lang.Long lastProcessedRecordId) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEvent[] MTNN_GetDailyEvents(java.util.Calendar date) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.POI_Trip[] sigla_GetPOITrips(java.lang.String user, java.lang.String password, java.util.Calendar startDateTime, java.util.Calendar endDateTime) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitConecta[] conecta_GetUnitsInfo(java.lang.String user, java.lang.String password, java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEtisalat[] etisalat_GetUnitsInfo(java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException;
    public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitGuvision[] guvision_GetUnitsInfo(java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException;
}
