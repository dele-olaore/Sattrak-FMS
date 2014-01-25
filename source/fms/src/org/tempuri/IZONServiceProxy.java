package org.tempuri;

public class IZONServiceProxy implements org.tempuri.IZONService {
  private String _endpoint = null;
  private org.tempuri.IZONService iZONService = null;
  
  public IZONServiceProxy() {
    _initIZONServiceProxy();
  }
  
  public IZONServiceProxy(String endpoint) {
    _endpoint = endpoint;
    _initIZONServiceProxy();
  }
  
  private void _initIZONServiceProxy() {
    try {
      iZONService = (new org.tempuri.ZONServiceLocator()).getBasicHttpBinding_IZONService();
      if (iZONService != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)iZONService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)iZONService)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (iZONService != null)
      ((javax.xml.rpc.Stub)iZONService)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public org.tempuri.IZONService getIZONService() {
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService;
  }
  
  public java.lang.String galooli() throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.galooli();
  }
  
  public java.lang.String test(java.lang.String text) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.test(text);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsResult getUnits(java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.getUnits(lastUpdateTimeGMT);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitsDetailsResult getUnitsInfo() throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.getUnitsInfo();
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEvent[] zGetDailyEvents(java.util.Calendar date) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.zGetDailyEvents(date);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.SATTRACKUnitEventResult SATTRAK_GetDailyEvents(java.util.Calendar startdate, java.util.Calendar enddate, java.lang.Long lastProcessedRecordId) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.SATTRAK_GetDailyEvents(startdate, enddate, lastProcessedRecordId);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEvent[] MTNN_GetDailyEvents(java.util.Calendar date) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.MTNN_GetDailyEvents(date);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.POI_Trip[] sigla_GetPOITrips(java.lang.String user, java.lang.String password, java.util.Calendar startDateTime, java.util.Calendar endDateTime) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.sigla_GetPOITrips(user, password, startDateTime, endDateTime);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitConecta[] conecta_GetUnitsInfo(java.lang.String user, java.lang.String password, java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.conecta_GetUnitsInfo(user, password, lastUpdateTimeGMT);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitEtisalat[] etisalat_GetUnitsInfo(java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.etisalat_GetUnitsInfo(lastUpdateTimeGMT);
  }
  
  public org.datacontract.schemas._2004._07.ZONIntegrationWCFService.UnitGuvision[] guvision_GetUnitsInfo(java.util.Calendar lastUpdateTimeGMT) throws java.rmi.RemoteException{
    if (iZONService == null)
      _initIZONServiceProxy();
    return iZONService.guvision_GetUnitsInfo(lastUpdateTimeGMT);
  }
  
  
}