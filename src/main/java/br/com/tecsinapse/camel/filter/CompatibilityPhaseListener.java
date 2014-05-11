package br.com.tecsinapse.camel.filter;


import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;


public class CompatibilityPhaseListener implements PhaseListener {
	private static final long serialVersionUID = 1L;
	
	  @Override
	    public PhaseId getPhaseId() {
	        return PhaseId.RENDER_RESPONSE;
	    }

	    @Override
	    public void beforePhase(PhaseEvent event) {
	        final FacesContext facesContext = event.getFacesContext();
	        final HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
	        response.addHeader("X-UA-Compatible", "IE=edge,chrome=1");
	    }

	    @Override
	    public void afterPhase(PhaseEvent event) {
	    }
}
