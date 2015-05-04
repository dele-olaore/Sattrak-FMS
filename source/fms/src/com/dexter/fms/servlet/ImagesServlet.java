package com.dexter.fms.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dexter.fms.dao.GeneralDAO;
import com.dexter.fms.model.Advert;
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
	
	//private final String PERSISTENCE_UNIT_NAME = "fms";
    
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
			String details = String.valueOf(request.getPathInfo().substring(1)); // Gets string that goes after "/imageservlet/".
			
			String r_id = "", photo="";
			if(details.indexOf(":")>=0)
			{
				r_id = details.split(":")[0];
				photo = details.split(":")[1];
			}
			else if(details.indexOf("-")>=0)
			{
				r_id = details.split("-")[0];
				photo = details.split("-")[1];
			}
			
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
				
				GeneralDAO gDAO = new GeneralDAO();
				if(photo.equalsIgnoreCase("photo"))
				{
					try
					{
						Object obj = gDAO.find(PartnerUser.class, id);
						if(obj != null)
						{
							PartnerUser u = (PartnerUser)obj;
							if(u != null && u.getPersonel() != null && u.getPersonel().getPhoto() != null)
							{
								data = u.getPersonel().getPhoto();
							}
						}
					} catch(Exception ex){}
				}
				else if(photo.equalsIgnoreCase("personel"))
				{
					try
					{
						Object obj = gDAO.find(PartnerPersonel.class, id);
						if(obj != null)
						{
							PartnerPersonel pp = (PartnerPersonel)obj;
							if(pp != null && pp.getPhoto() != null)
								data = pp.getPhoto();
						}
					} catch(Exception ex){}
				}
				else if(photo.equalsIgnoreCase("vparam"))
				{
					try
					{
						Object obj = gDAO.find(VehicleParameters.class, id);
						if(obj != null)
						{
							VehicleParameters vps = (VehicleParameters)obj;
							if(vps != null && vps.getPhoto() != null)
								data = vps.getPhoto();
						}
					} catch(Exception ex){}
				}
				else if(photo.equalsIgnoreCase("partner"))
				{
					try
					{
						Object obj = gDAO.find(PartnerSetting.class, id);
						if(obj != null)
						{
							PartnerSetting ps = (PartnerSetting)obj;
								if(ps != null && ps.getLogo() != null)
									data = ps.getLogo();
						}
					} catch(Exception ex){}
				}
				else if(photo.equalsIgnoreCase("advert"))
				{
					try
					{
						Object obj = gDAO.find(Advert.class, id);
						if(obj != null)
						{
							Advert ad = (Advert)obj;
							if(ad != null && ad.getContent() != null)
								data = ad.getContent();
						}
					} catch(Exception ex){}
				}
				gDAO.destroy();
				
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
					else if(photo.equalsIgnoreCase("advert"))
					{
						// TODO: Maybe we should put a default advert here
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
