package com.dexter.fms.dao.fuelcard;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.fuelcard.BankRecord;
import com.dexter.fms.model.ref.Region;

public class BankRecordDAO
{
	private static final String PERSISTENCE_UNIT_NAME = "fms";
    private static EntityManagerFactory factory;
    private EntityManager em;
	
    private EntityTransaction utx;
    
    final Logger logger = Logger.getLogger("FuelMReport-BankRecordDAO");
    
    public BankRecordDAO()
    {
    	factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }
    
    public boolean createBankRecord(BankRecord br)
    {
    	boolean ret = false;
    	
    	try
    	{
    		utx = em.getTransaction();
            utx.begin();
            em.persist(br);
            try
            {
                em.flush();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
            utx.commit();
            ret = true;
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.SEVERE, "Persist failed for bank record. " + ex);
    	}
    	
    	return ret;
    }
    
    @SuppressWarnings("unchecked")
	public Vector<BankRecord> searchByRegion(Date d1, Date d2, Region region)
    {
    	Vector<BankRecord> list = new Vector<BankRecord>();
    	ArrayList<Vehicle> cars = new ArrayList<Vehicle>();
		if(region != null) {
			Vector<Vehicle> rcars = new Vector<Vehicle>();
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("region", region);
			params.put("active", true);
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null)
				rcars = (Vector<Vehicle>)obj;
			gDAO.destroy();
			cars.addAll(rcars);
		} else {
			Vector<Vehicle> rcars = new Vector<Vehicle>();
			GeneralDAO gDAO = new GeneralDAO();
			Hashtable<String, Object> params = new Hashtable<String, Object>();
			params.put("active", true);
			Object obj = gDAO.search("Vehicle", params);
			if(obj != null)
				rcars = (Vector<Vehicle>)obj;
			gDAO.destroy();
			cars.addAll(rcars);
		}
		
		for(Vehicle c : cars) {
			list.addAll(search(d1, d2, c));
		}
		
		return list;
    }
    
    @SuppressWarnings("unchecked")
	public Vector<BankRecord> search(Date d1, Date d2, Vehicle car)
    {
    	Vector<BankRecord> list = new Vector<BankRecord>();
    	
    	try
    	{
    		Query q = em.createQuery("Select e from BankRecord e where e.vehicle=:vehicle and e.tranStatus=:tranStatus and (e.tranTime between :stdt and :eddt) order by e.tranTime");
    		q.setParameter("vehicle", car);
    		q.setParameter("tranStatus", "Processed");
    		q.setParameter("stdt", d1);
    		q.setParameter("eddt", d2);
    		
    		list = (Vector<BankRecord>)q.getResultList();
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.SEVERE, "Retieve failed for bank record: " + ex);
    	}
    	
    	return list;
    }
    
    @SuppressWarnings("unchecked")
	public Vector<BankRecord> search(Date d1, Date d2)
    {
    	Vector<BankRecord> list = new Vector<BankRecord>();
    	
    	try
    	{
    		Query q = em.createQuery("Select e from BankRecord e where e.tranStatus=:tranStatus and (e.tranTime between :stdt and :eddt) order by e.tranTime");
    		q.setParameter("tranStatus", "Processed");
    		q.setParameter("stdt", d1);
    		q.setParameter("eddt", d2);
    		
    		list = (Vector<BankRecord>)q.getResultList();
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.SEVERE, "Retieve failed for bank record: " + ex);
    	}
    	
    	return list;
    }
}
