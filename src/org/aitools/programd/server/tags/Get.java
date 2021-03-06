package org.aitools.programd.server.tags;

import java.io.IOException;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.aitools.programd.Core;
import org.aitools.programd.predicates.PredicateManager;

/**
 * Provides the version of the engine via JSP.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Get extends SimpleTagSupport {

  /** The name of the desired predicate. */
  protected String name;

  /** The PredicateMaster from which values will be gotten. */
  protected PredicateManager predicateMaster;

  /** The id of the bot for which to get the predicate value. */
  protected String botid;

  /** The id of the user for whom to get the predicate value. */
  protected String userid;

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws IOException {
    this.getJspContext().getOut().write(this.predicateMaster.get(this.name, this.userid, this.botid));
  }

  /**
   * Sets the name attribute.
   * 
   * @param value
   */
  public void setName(String value) {
    this.name = value;
    this.predicateMaster = ((Core) this.getJspContext().getAttribute("core", PageContext.APPLICATION_SCOPE))
        .getPredicateMaster();
    this.botid = ((org.aitools.programd.server.BotAccess) this.getJspContext().getAttribute("bot",
        PageContext.SESSION_SCOPE)).getBotId();
    this.userid = (String) this.getJspContext().getAttribute("userid", PageContext.SESSION_SCOPE);
  }
}
