package no.nav.vedtak.log.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Auditlogger {

    private static final Logger logger = LoggerFactory.getLogger("audit");
   

    public void logg(Auditdata auditdata) {
        logger.info(auditdata.toString());
    }
    
}
