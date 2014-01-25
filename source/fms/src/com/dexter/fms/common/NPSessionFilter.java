package com.dexter.fms.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(servletNames = "Faces Servlet")
public class NPSessionFilter implements Filter
{
	// The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    @SuppressWarnings("unused")
	private FilterConfig filterConfig = null;
    
    public NPSessionFilter()
    {}
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)throws IOException, ServletException 
    {
    	HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);
        
        String userAgent = request.getHeader("user-agent");
        String accept = request.getHeader("Accept");

        boolean isMobile = false;
        if (userAgent != null && accept != null)
        {
            UserAgentInfo agent = new UserAgentInfo(userAgent, accept);
            if (agent.isMobileDevice())
            {
            	isMobile = true;
            }
        }
        
        if(!request.getRequestURI().endsWith("index.xhtml") &&// !request.getRequestURI().endsWith("dashboard.xhtml") && 
        		!request.getRequestURI().endsWith("setup.xhtml") && (session == null || (session.getAttribute("loggedIn") != null && !(Boolean)session.getAttribute("loggedIn"))))
        {
        	if(!isMobile)
        		response.sendRedirect(request.getContextPath() + "/faces/index.xhtml"); // Redirect to login page.
        	else
        		response.sendRedirect(request.getContextPath() + "/faces/mindex.xhtml"); // Redirect to mobile login page.
        }
        else
        {
            chain.doFilter(req, res); // Bean is present in session, so just continue request.
        }
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) 
    {
    	this.filterConfig = filterConfig;
    }

	@Override
	public void destroy()
	{}
}
