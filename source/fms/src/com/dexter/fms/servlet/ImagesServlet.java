package com.dexter.fms.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dexter.fms.model.PartnerPersonel;
import com.dexter.fms.model.PartnerSetting;
import com.dexter.fms.model.PartnerUser;
import com.dexter.fms.model.app.VehicleParameters;

/**
 * Servlet implementation class ImagesServlet
 */
@WebServlet(description = "Servlet to render images", urlPatterns = { "/imageservlet/*" })
public class ImagesServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
	private static final String PERSISTENCE_UNIT_NAME = "fms";
    private static EntityManagerFactory factory;
    private EntityManager em;
    
    final Logger logger = Logger.getLogger("fms-ImagesServlet");
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImagesServlet()
    {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("deprecation")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
			em = factory.createEntityManager();
			
			String details = String.valueOf(request.getPathInfo().substring(1)); // Gets string that goes after "/imageservlet/".
			
			String r_id = details.split(":")[0];
			String photo = details.split(":")[1];
			Long id = 0L;
			try
			{
				try
				{
					id = Long.parseLong(r_id);
				}
				catch(Exception ig)
				{}
				
				byte[] data = null;
				
				if(photo.equalsIgnoreCase("photo"))
				{
					PartnerUser u = em.find(PartnerUser.class, id);
					if(u != null && u.getPersonel() != null && u.getPersonel().getPhoto() != null)
					{
						data = u.getPersonel().getPhoto();
					}
				}
				else if(photo.equalsIgnoreCase("personel"))
				{
					PartnerPersonel pp = em.find(PartnerPersonel.class, id);
					if(pp != null && pp.getPhoto() != null)
						data = pp.getPhoto();
				}
				else if(photo.equalsIgnoreCase("vparam"))
				{
					VehicleParameters vps = em.find(VehicleParameters.class, id);
					if(vps != null && vps.getPhoto() != null)
						data = vps.getPhoto();
				}
				else if(photo.equalsIgnoreCase("partner"))
				{
					PartnerSetting ps = em.find(PartnerSetting.class, id);
						if(ps != null && ps.getLogo() != null)
							data = ps.getLogo();
				}
				
				if(data != null)
				{
					response.setHeader("Content-Type", getServletContext().getMimeType("image/jpeg"));
			        response.setHeader("Content-Disposition", "inline; filename=\"photo\"");
	
			        BufferedInputStream input = null;
			        BufferedOutputStream output = null;
	
			        try
			        {
			        	input = new BufferedInputStream(new ByteArrayInputStream(data)); // Creates buffered input stream.
			        	
			            output = new BufferedOutputStream(response.getOutputStream());
			            byte[] buffer = new byte[8192];
			            for (int length = 0; (length = input.read(buffer)) > 0;) {
			                output.write(buffer, 0, length);
			            }
			        }
			        finally
			        {
			            if (output != null) try { output.close(); } catch (IOException logOrIgnore) {}
			            if (input != null) try { input.close(); } catch (IOException logOrIgnore) {}
			        }
				}
				else
				{
					File defaultIcon = null;
					if(photo.equalsIgnoreCase("partner"))
					{
						defaultIcon = new File(request.getRealPath("/resources/images/satraklogo.jpg"));
						response.setHeader("Content-Type", getServletContext().getMimeType("image/jpg"));
				        response.setHeader("Content-Disposition", "inline; filename=\"logo\"");
					}
					else
					{
						defaultIcon = new File(request.getRealPath("/resources/img/icons/16x16/user.png"));
						response.setHeader("Content-Type", getServletContext().getMimeType("image/png"));
				        response.setHeader("Content-Disposition", "inline; filename=\"photo\"");
					}
	
			        BufferedInputStream input = null;
			        BufferedOutputStream output = null;
	
			        try
			        {
			        	input = new BufferedInputStream(new FileInputStream(defaultIcon)); // Creates buffered input stream.
			        	
			            output = new BufferedOutputStream(response.getOutputStream());
			            byte[] buffer = new byte[8192];
			            for (int length = 0; (length = input.read(buffer)) > 0;) {
			                output.write(buffer, 0, length);
			            }
			        }
			        finally
			        {
			            if (output != null) try { output.close(); } catch (IOException logOrIgnore) {}
			            if (input != null) try { input.close(); } catch (IOException logOrIgnore) {}
			        }
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
			
			em.flush();
			em.close();
		}
		catch(Exception e){}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
