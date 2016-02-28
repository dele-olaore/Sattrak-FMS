package com.dexter.fms.dao.fuelcard;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.dexter.fms.model.fuelcard.CardBalanceNotification;

public class CardBalanceDAO
{
	private static final String PERSISTENCE_UNIT_NAME = "fms";
    private static EntityManagerFactory factory;
    private EntityManager em;
	
    private EntityTransaction utx;
    
    final Logger logger = Logger.getLogger("FuelMReport-CardBalanceDAO");
    
    public CardBalanceDAO()
    {
    	factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }
    
    public boolean setBalance(CardBalanceNotification bal)
    {
    	boolean ret = false;
    	
    	try
    	{
    		utx = em.getTransaction();
            utx.begin();
            em.persist(bal);
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
    		logger.log(Level.SEVERE, "Persist failed for card balance notification. " + ex);
    	}
    	
    	return ret;
    }
    
    public boolean updateBalance(CardBalanceNotification bal)
    {
    	boolean ret = false;
    	
    	try
    	{
    		utx = em.getTransaction();
            utx.begin();
            em.merge(bal);
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
    		logger.log(Level.SEVERE, "Persist failed for card balance notification. " + ex);
    	}
    	
    	return ret;
    }
    
    @SuppressWarnings("unchecked")
	public Vector<CardBalanceNotification> getSettings()
    {
    	Vector<CardBalanceNotification> list = new Vector<CardBalanceNotification>();
    	try
    	{
    		Query q = em.createQuery("Select e from CardBalanceNotification e");
    		Object obj = q.getResultList();
    		if(obj != null)
    			list = (Vector<CardBalanceNotification>) obj;
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.SEVERE, "Retieve failed for card notification settings. " + ex);
    	}
    	
    	return list;
    }
    
    @SuppressWarnings("unchecked")
	public CardBalanceNotification getBalanceNotification()
    {
    	CardBalanceNotification e = null;
    	
    	Vector<CardBalanceNotification> list = new Vector<CardBalanceNotification>();
    	
    	try
    	{
    		Query q = em.createQuery("Select e from CardBalanceNotification e");
    		Object obj = q.getResultList();
    		if(obj != null)
    			list = (Vector<CardBalanceNotification>) obj;
			
			e = list.get(0);
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.SEVERE, "Retieve failed for card notification balances. " + ex);
    	}
    	
    	return e;
    }
}
