package com.dexter.fms.dao.fuelcard;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.dexter.fms.model.app.Vehicle;
import com.dexter.fms.model.fuelcard.TrackerRecord;

public class TrackerRecordDAO
{
	private static final String PERSISTENCE_UNIT_NAME = "fms";
    private static EntityManagerFactory factory;
    private EntityManager em;
	
    private EntityTransaction utx;
    
    final Logger logger = Logger.getLogger("FuelMReport-TrackerRecordDAO");
    
    public TrackerRecordDAO()
    {
    	factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }
    
    public boolean createTrackerRecord(TrackerRecord tr)
    {
    	boolean ret = false;
    	
    	try
    	{
    		utx = em.getTransaction();
            utx.begin();
            em.persist(tr);
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
    		logger.log(Level.SEVERE, "Persist failed for tracker record. " + ex);
    	}
    	
    	return ret;
    }
    
    @SuppressWarnings("unchecked")
	public Vector<TrackerRecord> search(Date d1, Date d2, Vehicle car)
    {
    	Vector<TrackerRecord> list = new Vector<TrackerRecord>();
    	
    	try
    	{
    		Query q = em.createQuery("Select e from TrackerRecord e where e.vehicle=:vehicle and (e.tranTime between :stdt and :eddt) order by e.tranTime");
    		q.setParameter("vehicle", car);
    		q.setParameter("stdt", d1);
    		q.setParameter("eddt", d2);
    		
    		list = (Vector<TrackerRecord>)q.getResultList();
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.SEVERE, "Retieve failed for bank record: " + ex);
    	}
    	
    	return list;
    }
    
    @SuppressWarnings("unchecked")
	public Vector<TrackerRecord> search(Date d1, Date d2)
    {
    	Vector<TrackerRecord> list = new Vector<TrackerRecord>();
    	
    	try
    	{
    		Query q = em.createQuery("Select e from TrackerRecord e where (e.tranTime between :stdt and :eddt) order by e.tranTime");
    		q.setParameter("stdt", d1);
    		q.setParameter("eddt", d2);
    		
    		list = (Vector<TrackerRecord>)q.getResultList();
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.SEVERE, "Retieve failed for bank record: " + ex);
    	}
    	
    	return list;
    }
}
