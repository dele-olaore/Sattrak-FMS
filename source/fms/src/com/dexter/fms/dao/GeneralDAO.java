package com.dexter.fms.dao;

import java.util.Hashtable;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

public class GeneralDAO
{
	private static final String PERSISTENCE_UNIT_NAME = "fms";
    private static EntityManagerFactory factory;
    private EntityManager em;
	
    private EntityTransaction tx;
	
	private String message;
	private boolean hasError;
    
    final Logger logger = Logger.getLogger("fms-GeneralDAO");
    
    public GeneralDAO()
    {
    	factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        em = factory.createEntityManager();
    }
    
    public void startTransaction()
	{
    	setHasError(false);
		clean();
		
		tx = em.getTransaction();
		tx.begin();
	}
	
	public void commit()
	{
		tx.commit();
	}
	
	public void rollback()
	{
		tx.rollback();
	}
	
	public void destroy()
	{
		clean();
		try
		{
			em.close();
		}
		catch(Exception ex){}
	}
	
	public void clean()
	{
		setHasError(false);
		if(tx != null && tx.isActive())
		{
			try
			{
				tx.rollback();
			}
			catch(Exception ex){}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object find(Class cls, Long id)
	{
		return em.find(cls, id);
	}
	
	public Object findAll(String classname)
	{
		Query q = em.createQuery("Select e from " + classname + " e");
		q.setHint(QueryHints.REFRESH, HintValues.TRUE);
		return q.getResultList();
	}
	
	public Query createQuery(String str)
	{
		return em.createQuery(str);
	}
	
	public Object search(Query q, int count)
	{
		Object ret = null;
		try
		{
			q.setHint(QueryHints.REFRESH, HintValues.TRUE);
			if(count > 0)
			{
				if(count == 1)
					ret = q.getSingleResult();
				else
					ret = q.setMaxResults(count).getResultList();
			}
			else
				ret = q.getResultList();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public Object search(String classname, Hashtable<String, Object> params)
	{
		Object ret = null;
		StringBuilder strBuilder = new StringBuilder("Select e from " + classname + " e");
		if(params.size() > 0)
		{
			int i = 1;
			for(String e : params.keySet())
			{
				if(i == 1)
					strBuilder.append(" where e." + e + "= :p" + i);
				else
					strBuilder.append(" and e." + e + "= :p" + i);
				i+=1;
			}
		}
		
		Query q = em.createQuery(strBuilder.toString());
		if(params.size() > 0)
		{
			int i = 1;
			for(String e : params.keySet())
			{
				q.setParameter("p"+i, params.get(e));
				i+=1;
			}
		}
		try
		{
			q.setHint(QueryHints.REFRESH, HintValues.TRUE);
			ret = q.getResultList();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public Object runQuery(String query)
	{
		Query q = em.createQuery(query);
		q.setHint(QueryHints.REFRESH, HintValues.TRUE);
		return q.getResultList();
	}
	
	public boolean save(Object entity)
	{
		setMessage(null);
		boolean ret = false;
		
		try
		{
			em.persist(entity);
			ret = true;
		}
		catch(Exception ex)
		{
			setHasError(true);
			setMessage(ex.getMessage());
			ex.printStackTrace();
			ret = false;
		}
		
		return ret;
	}
	
	public boolean update(Object entity)
	{
		setMessage(null);
		boolean ret = false;
		
		try
		{
			em.merge(entity);
			ret = true;
		}
		catch(Exception ex)
		{
			setHasError(true);
			setMessage(ex.getMessage());
			ex.printStackTrace();
			ret = false;
		}
		
		return ret;
	}
	
	public boolean remove(Object entity)
	{
		setMessage(null);
		boolean ret = false;
		
		try
		{
			em.remove(entity);
			ret = true;
		}
		catch(Exception ex)
		{
			setHasError(true);
			setMessage(ex.getMessage());
			ex.printStackTrace();
			ret = false;
		}
		
		return ret;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isHasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}
	
}
